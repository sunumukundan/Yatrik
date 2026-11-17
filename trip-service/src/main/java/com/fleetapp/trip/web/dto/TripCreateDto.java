package com.fleetapp.trip.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record TripCreateDto(
        @NotNull UUID vehicleId,
        @NotNull UUID driverId,
        UUID partyId,
        @NotBlank String origin,
        @NotBlank String destination,
        LocalDate startDate,
        BigDecimal distanceKm,
        @PositiveOrZero long freightAmount,
        @PositiveOrZero long customerAdvance,
        @PositiveOrZero long driverAdvance,
        String notes,
        // Removed field. Spring ignores unknown JSON properties, so without this an old client
        // sending advanceAmount would get a 201 and silently lose its advance. @Null turns
        // that into a 400 that says what to do. Delete once no client sends it.
        @Null(message = "advanceAmount was split into customerAdvance and driverAdvance")
        Long advanceAmount
) {}
