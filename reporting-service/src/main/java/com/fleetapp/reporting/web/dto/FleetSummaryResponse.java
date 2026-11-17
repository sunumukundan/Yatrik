package com.fleetapp.reporting.web.dto;

/**
 * The fleet service's summary, as the reporting service sees it.
 */
public record FleetSummaryResponse(
        long totalVehicles,
        long activeVehicles,
        long idleVehicles,
        long inServiceVehicles,
        long retiredVehicles,
        long totalDrivers,
        long availableDrivers,
        long driversOnTrip,
        long openWorkOrders,
        long servicesDueSoon
) {}
