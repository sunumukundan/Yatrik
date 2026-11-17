package com.fleetapp.fleet.web.dto;

import com.fleetapp.fleet.domain.VehicleStatus;

import java.math.BigDecimal;

/**
 * Every field is optional: a null field is left unchanged. The registration number is
 * deliberately not editable — it is the vehicle's identity in every document, and
 * changing it silently would desynchronise the papers.
 */
public record VehicleUpdateDto(
        String makeModel,
        BigDecimal capacityTons,
        VehicleStatus status
) {}
