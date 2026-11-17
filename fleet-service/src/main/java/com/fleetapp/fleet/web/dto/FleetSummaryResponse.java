package com.fleetapp.fleet.web.dto;

/**
 * The headline numbers for the fleet, as shown on the dashboard.
 *
 * All money is in paise; counts are plain numbers.
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
