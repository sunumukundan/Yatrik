package com.fleetapp.fleet.web.dto;

import com.fleetapp.fleet.domain.MaintenanceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record MaintenanceCreateDto(
        @NotNull MaintenanceType type,
        @NotBlank String description,
        LocalDate dueDate,
        BigDecimal dueOdometerKm,
        String notes
) {}
