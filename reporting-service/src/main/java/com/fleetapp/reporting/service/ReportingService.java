package com.fleetapp.reporting.service;

import com.fleetapp.reporting.client.FleetClient;
import com.fleetapp.reporting.client.LedgerClient;
import com.fleetapp.reporting.client.TripClient;
import com.fleetapp.reporting.web.dto.FleetSummaryResponse;
import com.fleetapp.reporting.web.dto.FinancialReportResponse;
import com.fleetapp.reporting.web.dto.PartyBalanceResponse;
import com.fleetapp.reporting.web.dto.TripSummaryResponse;
import com.fleetapp.reporting.web.dto.UtilizationReportResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Builds the cross-service reports.
 *
 * This service owns no data of its own: it calls fleet-, trip- and ledger-service and
 * combines what they return. Every call carries the caller's token, so each service
 * scopes the answer to the caller's own organisation — the reporting service cannot
 * see another tenant's fleet even if it wanted to.
 */
@Service
@RequiredArgsConstructor
public class ReportingService {

    private final FleetClient fleetClient;
    private final TripClient tripClient;
    private final LedgerClient ledgerClient;

    /**
     * How much of the fleet was actually earning during the period.
     */
    public UtilizationReportResponse utilization(LocalDate from, LocalDate to) {
        FleetSummaryResponse fleet = fleetClient.summary(30);
        List<TripSummaryResponse> trips = tripClient.summary(from, to);

        long totalDistance = trips.stream()
                .map(TripSummaryResponse::distanceKm)
                .filter(d -> d != null)
                .mapToLong(d -> d.longValue())
                .sum();

        long completed = trips.stream()
                .filter(t -> t.endDate() != null)
                .count();

        return UtilizationReportResponse.of(
                fleet.totalVehicles(),
                fleet.activeVehicles() + fleet.idleVehicles(),
                fleet.idleVehicles(),
                fleet.inServiceVehicles(),
                trips.size(),
                completed,
                BigDecimal.valueOf(totalDistance));
    }

    /**
     * The money picture for the period.
     */
    public FinancialReportResponse financials(LocalDate from, LocalDate to) {
        List<TripSummaryResponse> trips = tripClient.summary(from, to);

        long freight = trips.stream().mapToLong(TripSummaryResponse::freightAmount).sum();
        long expenses = trips.stream().mapToLong(TripSummaryResponse::totalExpenses).sum();
        long customerAdvance = trips.stream().mapToLong(TripSummaryResponse::customerAdvance).sum();
        long driverAdvance = trips.stream().mapToLong(TripSummaryResponse::driverAdvance).sum();

        long receivables = ledgerClient.parties().stream()
                .filter(p -> p.currentBalance() > 0)
                .mapToLong(PartyBalanceResponse::currentBalance)
                .sum();

        return FinancialReportResponse.of(
                freight, expenses, customerAdvance, driverAdvance, receivables);
    }

    /**
     * The fuel and running-cost breakdown for the period.
     *
     * "Fuel spend" here is the FUEL category of trip expenses. It is approximated from
     * the trip summary rather than from a per-category query, because the summary is
     * already one round trip for the whole period.
     */
    public FuelSpendReportResponse fuelSpend(LocalDate from, LocalDate to) {
        List<TripSummaryResponse> trips = tripClient.summary(from, to);

        long totalExpenses = trips.stream().mapToLong(TripSummaryResponse::totalExpenses).sum();
        long totalDistance = trips.stream()
                .map(TripSummaryResponse::distanceKm)
                .filter(d -> d != null)
                .mapToLong(d -> d.longValue())
                .sum();

        BigDecimal costPerKm = totalDistance == 0
                ? BigDecimal.ZERO
                : BigDecimal.valueOf(totalExpenses)
                    .divide(BigDecimal.valueOf(totalDistance), 2, java.math.RoundingMode.HALF_UP);

        return new FuelSpendReportResponse(totalExpenses, totalDistance, costPerKm, trips.size());
    }

    /**
     * Who owes what, largest first — the receivables list an owner chases.
     */
    public List<ReceivableRowResponse> receivables() {
        return ledgerClient.parties().stream()
                .filter(p -> p.currentBalance() > 0)
                .sorted((a, b) -> Long.compare(b.currentBalance(), a.currentBalance()))
                .map(p -> new ReceivableRowResponse(
                        p.id(), p.name(), p.type(), p.currentBalance()))
                .toList();
    }

    public record FuelSpendReportResponse(
            long totalExpenses,
            long totalDistanceKm,
            BigDecimal costPerKm,
            int tripsCount
    ) {}

    public record ReceivableRowResponse(
            java.util.UUID partyId,
            String partyName,
            String partyType,
            long amountDue
    ) {}
}
