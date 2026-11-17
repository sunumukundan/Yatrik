package com.fleetapp.fleet.repository;

import com.fleetapp.fleet.domain.WorkOrder;
import com.fleetapp.fleet.domain.WorkOrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WorkOrderRepository extends JpaRepository<WorkOrder, UUID> {

    List<WorkOrder> findByVehicleIdAndDeletedAtIsNullOrderByRaisedOnDesc(UUID vehicleId);

    List<WorkOrder> findByOrganisationIdAndStatusAndDeletedAtIsNull(
            UUID organisationId, WorkOrderStatus status);

    List<WorkOrder> findByOrganisationIdAndDeletedAtIsNullOrderByRaisedOnDesc(UUID organisationId);

    Optional<WorkOrder> findByIdAndOrganisationIdAndDeletedAtIsNull(UUID id, UUID organisationId);
}
