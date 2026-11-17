package com.fleetapp.fleet.web.dto;

import com.fleetapp.fleet.domain.WorkOrderStatus;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record WorkOrderUpdateDto(
        WorkOrderStatus status,
        @PositiveOrZero Long costAmount,
        BigDecimal odometerKm
) {}
