package com.fleetapp.fleet.web.dto;

import com.fleetapp.fleet.domain.MaintenanceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record WorkOrderCreateDto(
        @NotNull MaintenanceType type,
        @NotBlank String description,
        UUID maintenanceId,
        LocalDate scheduledFor,
        BigDecimal odometerKm,
        @PositiveOrZero Long costAmount,
        String garageName,
        String notes
) {}
