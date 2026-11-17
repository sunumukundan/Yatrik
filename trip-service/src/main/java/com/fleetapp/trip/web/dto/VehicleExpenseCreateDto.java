package com.fleetapp.trip.web.dto;

import com.fleetapp.trip.domain.VehicleExpenseCategory;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;
import java.util.UUID;

public record VehicleExpenseCreateDto(
        @NotNull VehicleExpenseCategory category,
        @Positive long amount,
        LocalDate expenseDate,
        String note,
        UUID clientRef
) {}
