package com.fleetapp.fleet.web.dto;

import com.fleetapp.fleet.domain.DriverStatus;

import java.time.LocalDate;

/** Every field optional: a null field is left unchanged. */
public record DriverUpdateDto(
        String name,
        String phone,
        String licenceNo,
        LocalDate licenceExpiry,
        DriverStatus status
) {}
