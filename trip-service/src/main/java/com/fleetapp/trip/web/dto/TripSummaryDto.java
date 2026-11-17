package com.fleetapp.trip.web.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * One row of the trip summary: everything a report needs about a trip in a single
 * object, so a report can be built without a second call per trip.
 *
 * All money is in paise.
 */
public record TripSummaryDto(
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
