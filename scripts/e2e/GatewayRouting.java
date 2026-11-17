import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.UUID;

/**
 * Checks the gateway's routing, including the path that two services both claim.
 *
 * The vehicle-expense path is the interesting one: the fleet service owns
 * /vehicles/** and the trip service owns /vehicles/{id}/expenses. The gateway has to
 * send each to the right place, or the expense endpoints are simply unreachable.
 *
 * Run it with:
 *   java scripts/e2e/GatewayRouting.java <identityLogPath> [gatewayUrl]
 */
public class GatewayRouting {

    private static int passed = 0;
    private static int failed = 0;
    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10)).build();

    public static void main(String[] args) throws Exception {
        String logPath = args.length > 0 ? args[0] : "";
        String gw = args.length > 1 ? args[1] : "http://localhost:8080";
        String phone = "9876543210";

        System.out.println("=== gateway routing ===");
        System.out.println("Target: " + gw);

        // log in through the gateway
        send("POST", gw + "/auth/otp/request", "{\"phone\":\"" + phone + "\"}", null);
        Thread.sleep(1500);
        String otp = readOtp(logPath, phone);
        if (otp == null) {
            System.out.println("ABORT: no OTP in " + logPath);
            System.exit(1);
        }
        Res loginRes = res("POST", gw + "/auth/otp/verify",
                "{\"phone\":\"" + phone + "\",\"otp\":\"" + otp + "\",\"name\":\"Owner\",\"organisationName\":\"Sharma Transport\"}", null);
        String login = loginRes.body();
        check("the gateway routes /auth/otp/verify", loginRes.status() == 200, "got " + loginRes.status() + " " + login);

        String token = field(login, "accessToken");
        check("a token comes back through the gateway", token != null && !token.isBlank(), login);

        // a vehicle, through the gateway
        String regNo = "MH-15-GW-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        Res vehRes = res("POST", gw + "/vehicles",
                "{\"regNo\":\"" + regNo + "\",\"type\":\"TRUCK\",\"makeModel\":\"Tata 407\"}", token);
        String veh = vehRes.body();
        check("the gateway routes /vehicles to the fleet service", vehRes.status() == 201, "got " + vehRes.status() + " " + veh);
        String vehicleId = field(veh, "id");

        // the vehicle-expense path: the trip service owns it, the fleet service does not
        Res expRes = res("POST", gw + "/vehicles/" + vehicleId + "/expenses",
                "{\"category\":\"SERVICING\",\"amount\":25000,\"clientRef\":\"" + UUID.randomUUID() + "\"}", token);
        String exp = expRes.body();
        check("the gateway routes /vehicles/{id}/expenses to the TRIP service (201)",
                expRes.status() == 201, "got " + expRes.status() + " " + exp);

        Res expList = res("GET", gw + "/vehicles/" + vehicleId + "/expenses", null, token);
        check("the vehicle's expenses can be read back",
                expList.status() == 200 && expList.body().contains("25000"),
                "got " + expList.status() + " " + expList.body());

        // the other routes
        check("the gateway routes /drivers", status("GET", gw + "/drivers", null, token) == 200, "");
        check("the gateway routes /maintenance", status("GET", gw + "/maintenance", null, token) == 200, "");
        check("the gateway routes /work-orders/open", status("GET", gw + "/work-orders/open", null, token) == 200, "");
        check("the gateway routes /fleet/summary", status("GET", gw + "/fleet/summary", null, token) == 200, "");
        check("the gateway routes /compliance", status("GET", gw + "/compliance/vehicle-documents/expired", null, token) == 200, "");
        check("the gateway routes /trips", status("GET", gw + "/trips", null, token) == 200, "");
        check("the gateway routes /trips/summary", status("GET", gw + "/trips/summary", null, token) == 200, "");
        check("the gateway routes /parties", status("GET", gw + "/parties", null, token) == 200, "");
        check("the gateway routes /reminders", status("GET", gw + "/reminders", null, token) == 200, "");
        check("the gateway routes /reports/utilization", status("GET", gw + "/reports/utilization", null, token) == 200, "");

        // the gateway does not authenticate: it is the services that check the token
        int noToken = status("GET", gw + "/vehicles", null, null);
        check("the downstream service rejects an unauthenticated call",
                noToken == 401, "got " + noToken + " (expected 401 from the service behind the gateway)");

        System.out.println();
        System.out.println("Passed: " + passed + "   Failed: " + failed);
        if (failed > 0) {
            System.exit(1);
        }
    }

    private static String readOtp(String logPath, String phone) {
        try {
            String found = null;
            for (String line : Files.readAllLines(Path.of(logPath))) {
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
        } catch (Exception e) {
            return null;
        }
    }

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

    private static int status(String method, String url, String body, String token) throws Exception {
        return res(method, url, body, token).status();
    }

    private static String send(String method, String url, String body, String token) throws Exception {
        return res(method, url, body, token).body();
    }

    /** The HTTP status and the body of one request. */
    private record Res(int status, String body) {}

    private static Res res(String method, String url, String body, String token) throws Exception {
        HttpRequest.Builder b = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(20)).header("Content-Type", "application/json");
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
