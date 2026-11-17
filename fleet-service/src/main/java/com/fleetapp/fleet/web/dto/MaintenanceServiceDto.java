package com.fleetapp.fleet.web.dto;

import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Records that a scheduled service was carried out.
 *
 * odometerKm becomes the vehicle's new service baseline, which is what the next
 * mileage-based schedule is measured from.
 */
public record MaintenanceServiceDto(
        LocalDate servicedOn,
        @PositiveOrZero BigDecimal odometerKm,
        @PositiveOrZero Long costAmount,
        String garageName
) {}
