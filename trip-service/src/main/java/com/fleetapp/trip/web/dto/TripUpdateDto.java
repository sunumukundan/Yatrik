package com.fleetapp.trip.web.dto;

import com.fleetapp.trip.domain.TripStatus;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Every field optional: a null field is left unchanged.
 *
 * The route, the vehicle and the driver are NOT here on purpose — they identify the
 * trip, and changing them after the fact would desynchronise the paperwork and the
 * ledger entry the trip has already produced.
 */
public record TripUpdateDto(
        BigDecimal distanceKm,
        LocalDate endDate,
        @PositiveOrZero Long freightAmount,
        @PositiveOrZero Long customerAdvance,
        @PositiveOrZero Long driverAdvance,
        String notes,
        TripStatus status
) {}
