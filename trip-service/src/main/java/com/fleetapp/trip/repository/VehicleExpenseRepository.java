package com.fleetapp.trip.repository;

import com.fleetapp.trip.domain.VehicleExpense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface VehicleExpenseRepository extends JpaRepository<VehicleExpense, UUID> {

    List<VehicleExpense> findByVehicleIdAndDeletedAtIsNullOrderByExpenseDateDesc(UUID vehicleId);

    @Query("select coalesce(sum(e.amount), 0) from VehicleExpense e "
            + "where e.vehicleId = :vehicleId and e.deletedAt is null")
    long sumAmountByVehicleId(UUID vehicleId);

    List<VehicleExpense> findByOrganisationIdAndExpenseDateBetweenAndDeletedAtIsNull(
            UUID organisationId, LocalDate from, LocalDate to);
}
