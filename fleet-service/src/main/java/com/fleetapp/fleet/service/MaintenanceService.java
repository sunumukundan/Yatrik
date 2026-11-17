package com.fleetapp.fleet.service;

import com.fleetapp.common.security.CurrentUser;
import com.fleetapp.fleet.domain.MaintenanceRecord;
import com.fleetapp.fleet.domain.ServiceScheduleStatus;
import com.fleetapp.fleet.domain.Vehicle;
import com.fleetapp.fleet.domain.VehicleStatus;
import com.fleetapp.fleet.domain.WorkOrder;
import com.fleetapp.fleet.domain.WorkOrderStatus;
import com.fleetapp.fleet.exception.InvalidMaintenanceException;
import com.fleetapp.fleet.repository.MaintenanceRecordRepository;
import com.fleetapp.fleet.repository.WorkOrderRepository;
import com.fleetapp.fleet.web.dto.MaintenanceCreateDto;
import com.fleetapp.fleet.web.dto.MaintenanceServiceDto;
import com.fleetapp.fleet.web.dto.WorkOrderCreateDto;
import com.fleetapp.fleet.web.dto.WorkOrderUpdateDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Maintenance: the schedule of planned work and the work orders that execute it.
 *
 * The two are deliberately separate. A schedule entry says "this vehicle is due for a
 * service on this date or at this odometer"; a work order says "this specific piece of
 * work is being done now". A breakdown has a work order but no schedule entry; a
 * service booked months ahead has a schedule entry and no work order until the day.
 */
@Service
@RequiredArgsConstructor
public class MaintenanceService {

    private final MaintenanceRecordRepository maintenanceRepository;
    private final WorkOrderRepository workOrderRepository;
    private final VehicleService vehicleService;

    // ---------------------------------------------------------------- schedule

    public List<MaintenanceRecord> listForVehicle(UUID vehicleId) {
        vehicleService.get(vehicleId); // ownership check
        return maintenanceRepository.findByVehicleIdAndDeletedAtIsNullOrderByDueDateAsc(vehicleId);
    }

    public List<MaintenanceRecord> listAll() {
        return maintenanceRepository.findByOrganisationIdAndDeletedAtIsNullOrderByDueDateAsc(
                CurrentUser.get().organisationId());
    }

    public MaintenanceRecord get(UUID id) {
        return maintenanceRepository.findById(id)
                .filter(m -> m.getOrganisationId().equals(CurrentUser.get().organisationId()))
                .filter(m -> m.getDeletedAt() == null)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException(
                        "Maintenance record not found: " + id));
    }

    @Transactional
    public MaintenanceRecord schedule(UUID vehicleId, MaintenanceCreateDto dto) {
        vehicleService.get(vehicleId); // ownership check

        // A schedule with neither a date nor an odometer threshold can never become
        // due, so it would sit in the list forever and never be actionable.
        if (dto.dueDate() == null && dto.dueOdometerKm() == null) {
            throw new InvalidMaintenanceException(
                    "A maintenance schedule needs a dueDate or a dueOdometerKm");
        }

        MaintenanceRecord record = new MaintenanceRecord();
        record.setOrganisationId(CurrentUser.get().organisationId());
        record.setVehicleId(vehicleId);
        record.setType(dto.type());
        record.setDescription(dto.description());
        record.setDueDate(dto.dueDate());
        record.setDueOdometerKm(dto.dueOdometerKm());
        record.setStatus(ServiceScheduleStatus.PENDING);
        record.setNotes(dto.notes());
        record.setCreatedBy(CurrentUser.get().userId());
        return maintenanceRepository.save(record);
    }

    /**
     * Marks a scheduled service as done.
     *
     * The service reading becomes the vehicle's new baseline for mileage-based
     * schedules — without that, the next service would be measured from a stale
     * odometer and would come due too early.
     */
    @Transactional
    public MaintenanceRecord markServiced(UUID id, MaintenanceServiceDto dto) {
        MaintenanceRecord record = get(id);
        if (record.getStatus() == ServiceScheduleStatus.SERVICED) {
            throw new InvalidMaintenanceException("This service has already been marked as done");
        }

        record.setStatus(ServiceScheduleStatus.SERVICED);
        record.setServicedOn(dto.servicedOn() != null ? dto.servicedOn() : LocalDate.now());
        record.setServicedAtOdometerKm(dto.odometerKm());
        if (dto.costAmount() != null) {
            record.setCostAmount(dto.costAmount());
        }
        if (dto.garageName() != null) {
            record.setGarageName(dto.garageName());
        }
        record = maintenanceRepository.save(record);

        if (dto.odometerKm() != null) {
            Vehicle vehicle = vehicleService.get(record.getVehicleId());
            vehicle.setLastServiceOdometerKm(dto.odometerKm());
            vehicleService.save(vehicle);
        }

        return record;
    }

    // ---------------------------------------------------------------- work orders

    public List<WorkOrder> listWorkOrders(UUID vehicleId) {
        vehicleService.get(vehicleId); // ownership check
        return workOrderRepository.findByVehicleIdAndDeletedAtIsNullOrderByRaisedOnDesc(vehicleId);
    }

    public List<WorkOrder> listOpenWorkOrders() {
        return workOrderRepository.findByOrganisationIdAndStatusAndDeletedAtIsNull(
                CurrentUser.get().organisationId(), WorkOrderStatus.OPEN);
    }

    /**
     * Raises a work order and takes the vehicle off the road.
     *
     * The vehicle moves to IN_SERVICE as soon as the order is raised, not when the
     * work starts: from the fleet's point of view a vehicle with an open order is
     * already unavailable, and the utilisation report counts that downtime.
     */
    @Transactional
    public WorkOrder raiseWorkOrder(UUID vehicleId, WorkOrderCreateDto dto) {
        Vehicle vehicle = vehicleService.get(vehicleId);

        if (dto.maintenanceId() != null) {
            MaintenanceRecord source = get(dto.maintenanceId());
            if (!source.getVehicleId().equals(vehicleId)) {
                throw new InvalidMaintenanceException(
                        "That maintenance schedule belongs to a different vehicle");
            }
        }

        WorkOrder order = new WorkOrder();
        order.setOrganisationId(CurrentUser.get().organisationId());
        order.setVehicleId(vehicleId);
        order.setMaintenanceId(dto.maintenanceId());
        order.setType(dto.type());
        order.setDescription(dto.description());
        order.setStatus(WorkOrderStatus.OPEN);
        order.setRaisedOn(LocalDate.now());
        order.setScheduledFor(dto.scheduledFor());
        order.setOdometerKm(dto.odometerKm());
        order.setCostAmount(dto.costAmount());
        order.setGarageName(dto.garageName());
        order.setNotes(dto.notes());
        order.setCreatedBy(CurrentUser.get().userId());
        order = workOrderRepository.save(order);

        if (vehicle.getStatus() != VehicleStatus.RETIRED) {
            vehicle.setStatus(VehicleStatus.IN_SERVICE);
            vehicleService.save(vehicle);
        }

        return order;
    }

    /**
     * Moves a work order along, and puts the vehicle back into service when the last
     * open order on it is finished.
     *
     * The vehicle is only returned to the fleet when NO work order on it is still open.
     * A vehicle with two open orders and one completed is still off the road — putting
     * it back would make it look available while a job is still outstanding.
     */
    @Transactional
    public WorkOrder updateWorkOrder(UUID id, WorkOrderUpdateDto dto) {
        WorkOrder order = workOrderRepository.findByIdAndOrganisationIdAndDeletedAtIsNull(
                        id, CurrentUser.get().organisationId())
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException(
                        "Work order not found: " + id));

        if (order.getStatus() == WorkOrderStatus.COMPLETED
                || order.getStatus() == WorkOrderStatus.CANCELLED) {
            throw new InvalidMaintenanceException(
                    "This work order is already closed and can no longer be changed");
        }

        WorkOrderStatus newStatus = dto.status() != null ? dto.status() : order.getStatus();
        order.setStatus(newStatus);

        if (newStatus == WorkOrderStatus.IN_PROGRESS && order.getStartedAt() == null) {
            order.setStartedAt(Instant.now());
        }
        if (newStatus == WorkOrderStatus.COMPLETED) {
            order.setCompletedAt(Instant.now());
            if (dto.costAmount() != null) {
                order.setCostAmount(dto.costAmount());
            }
            if (dto.odometerKm() != null) {
                order.setOdometerKm(dto.odometerKm());
            }
        }
        order = workOrderRepository.save(order);

        if (newStatus == WorkOrderStatus.COMPLETED || newStatus == WorkOrderStatus.CANCELLED) {
            releaseVehicleIfNoOpenWork(order.getVehicleId());
        }

        return order;
    }

    /**
     * Puts the vehicle back into the fleet once nothing is outstanding on it.
     * A vehicle that was RETIRED stays retired — completing a repair never revives it.
     */
    private void releaseVehicleIfNoOpenWork(UUID vehicleId) {
        boolean stillOpen = workOrderRepository.findByVehicleIdAndDeletedAtIsNullOrderByRaisedOnDesc(vehicleId)
                .stream()
                .anyMatch(w -> w.getStatus() == WorkOrderStatus.OPEN
                        || w.getStatus() == WorkOrderStatus.IN_PROGRESS);
        if (stillOpen) {
            return;
        }

        Vehicle vehicle = vehicleService.get(vehicleId);
        if (vehicle.getStatus() == VehicleStatus.IN_SERVICE) {
            vehicle.setStatus(VehicleStatus.ACTIVE);
            vehicleService.save(vehicle);
        }
    }

    /**
     * Services falling due inside the window — the "due soon" list the owner sees.
     */
    public List<MaintenanceRecord> dueWithin(int days) {
        LocalDate from = LocalDate.now();
        LocalDate to = from.plusDays(days);
        return maintenanceRepository.findByOrganisationIdAndStatusAndDueDateBetweenAndDeletedAtIsNull(
                CurrentUser.get().organisationId(), ServiceScheduleStatus.PENDING, from, to);
    }

    /**
     * The odometer threshold for a mileage-based service, i.e. how many more
     * kilometres the vehicle has to run before it is due. Null when the service is not
     * mileage-based, or when no current odometer reading is known.
     */
    public BigDecimal remainingKm(MaintenanceRecord record) {
        if (record.getDueOdometerKm() == null) {
            return null;
        }
        Vehicle vehicle = vehicleService.get(record.getVehicleId());
        BigDecimal last = vehicle.getLastServiceOdometerKm();
        if (last == null) {
            return null;
        }
        return record.getDueOdometerKm().subtract(last);
    }
}
