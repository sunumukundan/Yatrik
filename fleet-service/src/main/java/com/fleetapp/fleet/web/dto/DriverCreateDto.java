package com.fleetapp.fleet.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;

public record DriverCreateDto(
        @NotBlank String name,
        @NotBlank @Pattern(regexp = "^\\+?[0-9]{10,15}$") String phone,
        String licenceNo,
        LocalDate licenceExpiry
) {}
