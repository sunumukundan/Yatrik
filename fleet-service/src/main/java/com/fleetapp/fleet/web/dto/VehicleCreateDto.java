package com.fleetapp.fleet.web.dto;

import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

public record VehicleCreateDto(
        @NotBlank String regNo,
        @NotBlank String type,
        String makeModel,
        BigDecimal capacityTons
) {}
