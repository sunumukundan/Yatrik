import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

/**
 * End-to-end check of the fleet platform, run against live services.
 *
 * Same checks as scripts/test-trips.ps1, but as a single-file Java program so it runs
 * anywhere a JDK is present — including machines whose PowerShell execution policy
 * blocks .ps1 files.
 *
 * Run it with:
 *   java scripts/e2e/FleetE2E.java <identityLogPath> [identityUrl] [fleetUrl] [tripUrl] [ledgerUrl]
 *
 * The OTP is printed to identity-service's console. This program reads it back out of
 * that log rather than taking it as an argument, so the code never sits in a shell
 * history or a process listing. Point it at the log file identity-service writes to:
 *
 *   java -jar identity-service/target/identity-service.jar > /tmp/identity.log &
 *   java scripts/e2e/FleetE2E.java /tmp/identity.log
 */
public class FleetE2E {

    private static int passed = 0;
    private static int failed = 0;
    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    record Res(int status, String body) {
        String json() {
            int s = body.indexOf('{');
            int e = body.lastIndexOf('}');
            return (s >= 0 && e > s) ? body.substring(s, e + 1) : body;
        }
    }

    public static void main(String[] args) throws Exception {
        String logPath = args.length > 0 ? args[0] : "";
        String identity = args.length > 1 ? args[1] : "http://localhost:8081";
        String fleet = args.length > 2 ? args[2] : "http://localhost:8082";
        String trip = args.length > 3 ? args[3] : "http://localhost:8083";
        String ledger = args.length > 4 ? args[4] : "http://localhost:8084";
        String phone = "9876543210";

        // A per-run tag, so a repeated run against the same database does not collide
        // with the unique registration number, phone number and GST number it
        // created last time.
        String runTag = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        // The driver's phone has to be 10-15 digits, so it is built from digits only.
        String driverPhone = "9" + (Math.abs(UUID.randomUUID().getMostSignificantBits() % 1000000000L) + 1000000000L);

        System.out.println("=== fleet / trip / ledger end-to-end ===");

        // 1. log in ---------------------------------------------------------------
        Res req = send("POST", identity + "/auth/otp/request", "{\"phone\":\"" + phone + "\"}", null);
        check("OTP request accepted (202)", req.status() == 202, "got " + req.status());

        String otp = readOtp(logPath, phone);
        if (otp == null) {
            System.out.println("ABORT: no OTP for " + phone + " in " + logPath
                    + " — is identity-service writing its log there?");
            System.exit(1);
        }

        Res login = send("POST", identity + "/auth/otp/verify",
                "{\"phone\":\"" + phone + "\",\"otp\":\"" + otp + "\",\"name\":\"Owner\",\"organisationName\":\"Sharma Transport\"}", null);
        if (login.status() != 200) {
            System.out.println("ABORT: login failed: " + login.status() + " " + login.body());
            System.exit(1);
        }
        String token = field(login.json(), "accessToken");
        check("access token returned", token != null && !token.isBlank(), login.json());
        check("organisation id returned", field(login.json(), "organisationId") != null, login.json());
        check("role returned", "OWNER".equals(field(login.json(), "role")), login.json());

        // 2. the OTP is single-use ------------------------------------------------
        Res replay = send("POST", identity + "/auth/otp/verify",
                "{\"phone\":\"" + phone + "\",\"otp\":\"" + otp + "\"}", null);
        check("a replayed OTP is rejected", replay.status() != 200, "got " + replay.status());

        // 3. authentication is required -------------------------------------------
        Res noToken = send("GET", fleet + "/vehicles", null, null);
        check("no token -> 401", noToken.status() == 401, "got " + noToken.status());

        Res badToken = send("GET", fleet + "/vehicles", null, "not-a-real-token");
        check("a bad token -> 401", badToken.status() == 401, "got " + badToken.status());

        // 4. create a vehicle -----------------------------------------------------
        // The registration number is unique per organisation, so each run uses its own.
        // That way the script can be run repeatedly against the same database.
        String regNo = "MH-12-AB-" + runTag;
        Res veh = send("POST", fleet + "/vehicles", "{\"regNo\":\"" + regNo + "\",\"type\":\"TRUCK\","
                + "\"makeModel\":\"Tata 407\",\"capacityTons\":2.5}", token);
        check("vehicle created (201)", veh.status() == 201, "got " + veh.status() + " " + veh.body());
        String vehicleId = field(veh.json(), "id");
        check("vehicle has an id", vehicleId != null, veh.body());

        Res vehList = send("GET", fleet + "/vehicles", null, token);
        check("the vehicle is listed", vehIdAppears(vehList.body(), vehicleId), vehList.body());

        Res byStatus = send("GET", fleet + "/vehicles/by-status/IDLE", null, token);
        check("the vehicle is listed as IDLE", vehIdAppears(byStatus.body(), vehicleId), byStatus.body());

        // 5. create a driver ------------------------------------------------------
        Res drv = send("POST", fleet + "/drivers", "{\"name\":\"Ramesh Kumar\",\"phone\":\"" + driverPhone + "\","
                + "\"licenceNo\":\"MH14201999" + runTag + "\",\"licenceExpiry\":\"2030-12-31\"}", token);
        check("driver created (201)", drv.status() == 201, "got " + drv.status() + " " + drv.body());
        String driverId = field(drv.json(), "id");

        // 6. create a customer party -----------------------------------------------
        Res pty = send("POST", ledger + "/parties", "{\"name\":\"ABC Logistics " + runTag + "\",\"phone\":\"9822000011\","
                + "\"type\":\"CUSTOMER\",\"gstNo\":\"27AAAAA" + runTag + "A1Z5\",\"openingBalance\":0}", token);
        check("party created (201)", pty.status() == 201, "got " + pty.status() + " " + pty.body());
        String partyId = field(pty.json(), "id");

        // 7. create a trip ---------------------------------------------------------
        // freight 50,000.00; customer advance 10,000.00; driver advance 2,000.00 -- all in paise
        Res t = send("POST", trip + "/trips", "{\"vehicleId\":\"" + vehicleId + "\",\"driverId\":\"" + driverId
                + "\",\"partyId\":\"" + partyId + "\",\"origin\":\"Mumbai\",\"destination\":\"Pune\","
                + "\"startDate\":\"2026-01-15\",\"distanceKm\":150.5,\"freightAmount\":5000000,"
                + "\"customerAdvance\":1000000,\"driverAdvance\":200000}", token);
        check("trip created (201)", t.status() == 201, "got " + t.status() + " " + t.body());
        String tripId = field(t.json(), "id");
        check("freight is stored in paise", t.body().contains("\"freightAmount\":5000000"), t.body());
        check("customer advance is stored separately", t.body().contains("\"customerAdvance\":1000000"), t.body());
        check("driver advance is stored separately", t.body().contains("\"driverAdvance\":200000"), t.body());

        // 8. the removed advanceAmount field is rejected ---------------------------
        Res legacy = send("POST", trip + "/trips", "{\"vehicleId\":\"" + vehicleId + "\",\"driverId\":\"" + driverId
                + "\",\"origin\":\"Mumbai\",\"destination\":\"Pune\",\"advanceAmount\":500000}", token);
        check("the legacy advanceAmount -> 400", legacy.status() == 400, "got " + legacy.status());

        // 9. a trip expense, and the same one twice --------------------------------
        String clientRef = UUID.randomUUID().toString();
        String expenseBody = "{\"category\":\"FUEL\",\"amount\":750000,\"paidBy\":\"OWNER\","
                + "\"expenseDate\":\"2026-01-15\",\"clientRef\":\"" + clientRef + "\"}";
        Res exp = send("POST", trip + "/trips/" + tripId + "/expenses", expenseBody, token);
        check("expense created (201)", exp.status() == 201, "got " + exp.status() + " " + exp.body());

        Res expAgain = send("POST", trip + "/trips/" + tripId + "/expenses", expenseBody, token);
        check("the same clientRef is not charged twice", expAgain.status() != 201, "got " + expAgain.status());

        // 10. the profit adds up ---------------------------------------------------
        Res profit = send("GET", trip + "/trips/" + tripId + "/profit", null, token);
        check("profit is reported", profit.status() == 200, "got " + profit.status());
        check("freight is 5000000 paise", profit.body().contains("\"freightAmount\":5000000"), profit.body());
        check("expenses are 750000 paise", profit.body().contains("\"totalExpenses\":750000"), profit.body());
        check("profit is 4250000 paise", profit.body().contains("\"profit\":4250000"), profit.body());

        // 11. complete the trip ----------------------------------------------------
        Res done = send("POST", trip + "/trips/" + tripId + "/complete", null, token);
        check("trip completed (200)", done.status() == 200, "got " + done.status() + " " + done.body());
        check("the trip is COMPLETED", done.body().contains("\"status\":\"COMPLETED\""), done.body());
        check("the trip is closed", !done.body().contains("\"closedAt\":null"), done.body());

        // 12. a closed trip cannot be changed ---------------------------------------
        Res edit = send("PUT", trip + "/trips/" + tripId, "{\"distanceKm\":999}", token);
        check("editing a closed trip -> 409", edit.status() == 409, "got " + edit.status());

        Res reComplete = send("POST", trip + "/trips/" + tripId + "/complete", null, token);
        check("completing twice -> 409", reComplete.status() == 409, "got " + reComplete.status());

        // 13. the ledger got the freight --------------------------------------------
        Res entries = send("GET", ledger + "/parties/" + partyId + "/entries", null, token);
        check("the party's ledger entries are readable", entries.status() == 200, "got " + entries.status());
        check("the trip produced a ledger entry", vehIdAppears(entries.body(), tripId), entries.body());
        check("the freight was debited", entries.body().contains("\"entryType\":\"DEBIT\""), entries.body());
        check("the advance was credited", entries.body().contains("\"entryType\":\"CREDIT\""), entries.body());

        Res bal = send("GET", ledger + "/parties/" + partyId, null, token);
        check("the party balance is readable", bal.status() == 200, "got " + bal.status());
        // freight 5000000 debited, advance 1000000 credited -> the customer owes 4000000
        check("the balance nets out to 4000000 paise", bal.body().contains("\"currentBalance\":4000000"), bal.body());

        // 14. the maintenance flow ---------------------------------------------------
        Res sched = send("POST", fleet + "/vehicles/" + vehicleId + "/maintenance",
                "{\"type\":\"ROUTINE_SERVICE\",\"description\":\"Oil change\",\"dueDate\":\"2026-02-01\","
                        + "\"dueOdometerKm\":1000}", token);
        check("a service was scheduled (201)", sched.status() == 201, "got " + sched.status() + " " + sched.body());
        String schedId = field(sched.json(), "id");

        Res wo = send("POST", fleet + "/vehicles/" + vehicleId + "/work-orders",
                "{\"type\":\"REPAIR\",\"description\":\"Brake pads\",\"scheduledFor\":\"2026-01-20\","
                        + "\"odometerKm\":1200,\"costAmount\":150000,\"garageName\":\"SP Motors\"}", token);
        check("a work order was raised (201)", wo.status() == 201, "got " + wo.status() + " " + wo.body());

        Res open = send("GET", fleet + "/work-orders/open", null, token);
        check("the open work order is listed", vehIdAppears(open.body(), field(wo.json(), "id")), open.body());

        // 15. the reporting service aggregates across services ------------------------
        Res util = send("GET", "http://localhost:8085/reports/utilization?from=2026-01-01&to=2026-12-31", null, token);
        check("the utilisation report answers (200)", util.status() == 200, "got " + util.status() + " " + util.body());

        Res fin = send("GET", "http://localhost:8085/reports/financials?from=2026-01-01&to=2026-12-31", null, token);
        check("the financial report answers (200)", fin.status() == 200, "got " + fin.status() + " " + fin.body());

        Res recv = send("GET", "http://localhost:8085/reports/receivables", null, token);
        check("the receivables report answers (200)", recv.status() == 200, "got " + recv.status() + " " + recv.body());
        check("the receivable shows the customer's balance", recv.body().contains("4000000"), recv.body());

        // 16. tenant isolation --------------------------------------------------------
        // A second organisation gets its own token. It must not see the first
        // organisation's vehicle, and it must not be able to reach the first
        // organisation's trip by guessing its id.
        String otherPhone = "9876543211";
        Res otherReq = send("POST", identity + "/auth/otp/request", "{\"phone\":\"" + otherPhone + "\"}", null);
        check("a second organisation can request an OTP", otherReq.status() == 202, "got " + otherReq.status());

        String otherOtp = readOtp(logPath, otherPhone);
        if (otherOtp == null) {
            check("the second organisation's OTP was found in the log", false, "no OTP for " + otherPhone);
        } else {
            Res otherLogin = send("POST", identity + "/auth/otp/verify",
                    "{\"phone\":\"" + otherPhone + "\",\"otp\":\"" + otherOtp + "\","
                            + "\"name\":\"Other Owner\",\"organisationName\":\"Other Transport\"}", null);
            check("the second organisation can log in", otherLogin.status() == 200, "got " + otherLogin.status());
            String otherToken = field(otherLogin.json(), "accessToken");
            String otherOrg = field(otherLogin.json(), "organisationId");
            check("the second organisation has its own id", otherOrg != null && !otherOrg.equals(field(login.json(), "organisationId")),
                    otherOrg + " vs " + field(login.json(), "organisationId"));

            Res otherVehicles = send("GET", fleet + "/vehicles", null, otherToken);
            check("the second organisation cannot see the first's vehicle",
                    !vehIdAppears(otherVehicles.body(), vehicleId), otherVehicles.body());

            Res otherTrips = send("GET", trip + "/trips", null, otherToken);
            check("the second organisation cannot see the first's trip",
                    !vehIdAppears(otherTrips.body(), tripId), otherTrips.body());

            Res crossTrip = send("GET", trip + "/trips/" + tripId, null, otherToken);
            check("the first's trip is 404 for the second organisation",
                    crossTrip.status() == 404, "got " + crossTrip.status());
        }

        System.out.println();
        System.out.println("Passed: " + passed + "   Failed: " + failed);
        if (failed > 0) {
            System.exit(1);
        }
    }

    // ------------------------------------------------------------------ helpers

    /**
     * Reads the OTP identity-service printed for this phone number.
     *
     * The log line looks like:
     *   [DEV ONLY] OTP for 9876543210 is 123456 (expires in 5 min)
     *
     * The last match wins, so a re-requested OTP is the one that gets used.
     */
    private static String readOtp(String logPath, String phone) {
        try {
            List<String> lines = Files.readAllLines(Path.of(logPath));
            String found = null;
            for (String line : lines) {
                String marker = "OTP for " + phone + " is ";
                int i = line.indexOf(marker);
                if (i < 0) {
                    continue;
                }
                String rest = line.substring(i + marker.length()).trim();
                int end = 0;
                while (end < rest.length() && Character.isDigit(rest.charAt(end))) {
                    end++;
                }
                if (end == 6) {
                    found = rest.substring(0, end);
                }
            }
            return found;
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Whether a JSON body mentions this id. Null-safe on the id, so a failed create
     * reports its own failure rather than crashing the run.
     */
    private static boolean vehIdAppears(String json, String id) {
        return id != null && json != null && json.contains(id);
    }

    /**
     * A tiny JSON string-field reader. Deliberately not a JSON library: the responses
     * are flat objects with string values, and adding a dependency to a test script
     * is not worth it.
     */
    private static String field(String json, String name) {
        String key = "\"" + name + "\":\"";
        int i = json.indexOf(key);
        if (i < 0) {
            return null;
        }
        int start = i + key.length();
        int end = json.indexOf('"', start);
        return end < 0 ? null : json.substring(start, end);
    }

    private static Res send(String method, String url, String body, String token) throws Exception {
        HttpRequest.Builder b = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(20))
                .header("Content-Type", "application/json");
        if (token != null) {
            b.header("Authorization", "Bearer " + token);
        }
        b.method(method, body == null ? HttpRequest.BodyPublishers.noBody()
                : HttpRequest.BodyPublishers.ofString(body));
        HttpResponse<String> r = CLIENT.send(b.build(), HttpResponse.BodyHandlers.ofString());
        return new Res(r.statusCode(), r.body());
    }

    private static void check(String label, boolean ok, String detail) {
        if (ok) {
            passed++;
            System.out.println("  PASS  " + label);
        } else {
            failed++;
            System.out.println("  FAIL  " + label + " -- " + detail);
        }
    }
}
