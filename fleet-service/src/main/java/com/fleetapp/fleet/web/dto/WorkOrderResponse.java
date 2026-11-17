package com.fleetapp.fleet.web.dto;

import com.fleetapp.fleet.domain.WorkOrder;
import com.fleetapp.fleet.domain.MaintenanceType;
import com.fleetapp.fleet.domain.WorkOrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record WorkOrderResponse(
        UUID id, UUID vehicleId, UUID maintenanceId, MaintenanceType type, String description,
        WorkOrderStatus status, LocalDate raisedOn, LocalDate scheduledFor,
        Instant startedAt, Instant completedAt, BigDecimal odometerKm,
        Long costAmount, String garageName, String notes
) {
    public static WorkOrderResponse from(WorkOrder w) {
        return new WorkOrderResponse(w.getId(), w.getVehicleId(), w.getMaintenanceId(), w.getType(),
                w.getDescription(), w.getStatus(), w.getRaisedOn(), w.getScheduledFor(),
                w.getStartedAt(), w.getCompletedAt(), w.getOdometerKm(),
                w.getCostAmount(), w.getGarageName(), w.getNotes());
    }
}
