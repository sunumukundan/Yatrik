package com.fleetapp.trip.web.dto;

import com.fleetapp.trip.domain.VehicleExpense;
import com.fleetapp.trip.domain.VehicleExpenseCategory;

import java.time.LocalDate;
import java.util.UUID;

public record VehicleExpenseResponse(
        UUID id, UUID vehicleId, VehicleExpenseCategory category, long amount,
        LocalDate expenseDate, String note
) {
    public static VehicleExpenseResponse from(VehicleExpense e) {
        return new VehicleExpenseResponse(e.getId(), e.getVehicleId(), e.getCategory(), e.getAmount(),
                e.getExpenseDate(), e.getNote());
    }
}
