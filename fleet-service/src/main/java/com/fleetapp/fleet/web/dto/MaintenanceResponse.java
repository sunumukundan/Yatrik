package com.fleetapp.fleet.web.dto;

import com.fleetapp.fleet.domain.MaintenanceRecord;
import com.fleetapp.fleet.domain.MaintenanceType;
import com.fleetapp.fleet.domain.ServiceScheduleStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record MaintenanceResponse(
        UUID id, UUID vehicleId, MaintenanceType type, String description,
        LocalDate dueDate, BigDecimal dueOdometerKm, ServiceScheduleStatus status,
        LocalDate servicedOn, BigDecimal servicedAtOdometerKm,
        Long costAmount, String garageName, String notes
) {
    public static MaintenanceResponse from(MaintenanceRecord m) {
        return new MaintenanceResponse(m.getId(), m.getVehicleId(), m.getType(), m.getDescription(),
                m.getDueDate(), m.getDueOdometerKm(), m.getStatus(),
                m.getServicedOn(), m.getServicedAtOdometerKm(),
                m.getCostAmount(), m.getGarageName(), m.getNotes());
    }
}
