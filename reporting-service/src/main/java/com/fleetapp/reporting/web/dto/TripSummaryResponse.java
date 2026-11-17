package com.fleetapp.reporting.web.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * The trip service's period summary, as the reporting service sees it.
 * All money is in paise.
 */
public record TripSummaryResponse(
        UUID tripId,
        UUID vehicleId,
        UUID driverId,
        UUID partyId,
        LocalDate startDate,
        LocalDate endDate,
        BigDecimal distanceKm,
        long freightAmount,
        long customerAdvance,
        long driverAdvance,
        long totalExpenses,
        long profit,
        long spentByDriver
) {}
