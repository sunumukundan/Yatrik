package com.fleetapp.fleet.web.dto;

import com.fleetapp.fleet.domain.Vehicle;
import com.fleetapp.fleet.domain.VehicleStatus;

import java.math.BigDecimal;
import java.util.UUID;

public record VehicleResponse(
        UUID id, String regNo, String type, String makeModel,
        BigDecimal capacityTons, VehicleStatus status, UUID currentDriverId,
        BigDecimal lastServiceOdometerKm
) {
    public static VehicleResponse from(Vehicle v) {
        return new VehicleResponse(v.getId(), v.getRegNo(), v.getType(), v.getMakeModel(),
                v.getCapacityTons(), v.getStatus(), v.getCurrentDriverId(),
                v.getLastServiceOdometerKm());
    }
}
