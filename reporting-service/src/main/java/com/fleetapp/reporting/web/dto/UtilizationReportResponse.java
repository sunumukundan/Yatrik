package com.fleetapp.reporting.web.dto;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Fleet utilisation for a period: how much of the fleet was actually earning.
 *
 * utilisationPercent = tripsRun / vehiclesInFleet * 100. A vehicle that is off the
 * road for most of the period drags this down, which is the point — it is the number
 * that tells an owner whether buying another vehicle would help.
 */
public record UtilizationReportResponse(
        long totalVehicles,
        long vehiclesInService,
        long vehiclesIdle,
        long vehiclesOffRoad,
        long tripsRun,
        long completedTrips,
        BigDecimal totalDistanceKm,
        BigDecimal averageDistancePerTripKm,
        BigDecimal utilizationPercent
) {
    /**
     * Builds the report from the raw counts.
     *
     * Rounded to two decimals, because a utilisation figure with 17 decimal places is
     * noise, and the underlying data is not that precise anyway.
     */
    public static UtilizationReportResponse of(long totalVehicles, long vehiclesInService,
                                              long vehiclesIdle, long vehiclesOffRoad,
                                              long tripsRun, long completedTrips,
                                              BigDecimal totalDistanceKm) {
        BigDecimal average = tripsRun == 0
                ? BigDecimal.ZERO
                : totalDistanceKm.divide(BigDecimal.valueOf(tripsRun), 2, RoundingMode.HALF_UP);

        BigDecimal utilization = totalVehicles == 0
                ? BigDecimal.ZERO
                : BigDecimal.valueOf(tripsRun)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(totalVehicles), 2, RoundingMode.HALF_UP);

        return new UtilizationReportResponse(
                totalVehicles, vehiclesInService, vehiclesIdle, vehiclesOffRoad,
                tripsRun, completedTrips, totalDistanceKm, average, utilization);
    }
}
