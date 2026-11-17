package com.fleetapp.trip.service;

import com.fleetapp.common.security.CurrentUser;
import com.fleetapp.trip.domain.VehicleExpense;
import com.fleetapp.trip.repository.VehicleExpenseRepository;
import com.fleetapp.trip.web.dto.VehicleExpenseCreateDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Costs that belong to the vehicle, not to a trip: EMI, insurance, servicing.
 *
 * They are tracked here rather than in fleet-service because they are money
 * movements like trip expenses, and they are the numbers a fleet owner's
 * cost-per-km calculation needs.
 */
@Service
@RequiredArgsConstructor
public class VehicleExpenseService {

    private final VehicleExpenseRepository vehicleExpenseRepository;

    public List<VehicleExpense> list(UUID vehicleId) {
        return vehicleExpenseRepository.findByVehicleIdAndDeletedAtIsNullOrderByExpenseDateDesc(vehicleId);
    }

    @Transactional
    public VehicleExpense create(UUID vehicleId, VehicleExpenseCreateDto dto) {
        VehicleExpense expense = new VehicleExpense();
        expense.setOrganisationId(CurrentUser.get().organisationId());
        expense.setVehicleId(vehicleId);
        expense.setCategory(dto.category());
        expense.setAmount(dto.amount());
        if (dto.expenseDate() != null) {
            expense.setExpenseDate(dto.expenseDate());
        }
        expense.setNote(dto.note());
        expense.setClientRef(dto.clientRef());
        expense.setCreatedBy(CurrentUser.get().userId());
        return vehicleExpenseRepository.save(expense);
    }
}
