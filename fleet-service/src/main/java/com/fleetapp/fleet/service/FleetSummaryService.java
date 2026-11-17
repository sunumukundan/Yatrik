package com.fleetapp.fleet.service;

import com.fleetapp.common.security.CurrentUser;
import com.fleetapp.fleet.domain.DriverStatus;
import com.fleetapp.fleet.domain.VehicleStatus;
import com.fleetapp.fleet.domain.WorkOrderStatus;
import com.fleetapp.fleet.repository.DriverRepository;
import com.fleetapp.fleet.repository.VehicleRepository;
import com.fleetapp.fleet.repository.WorkOrderRepository;
import com.fleetapp.fleet.web.dto.FleetSummaryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * The dashboard's headline numbers.
 *
 * Everything is counted inside one organisation, so one tenant's fleet never shows up
 * in another's summary.
 */
@Service
@RequiredArgsConstructor
public class FleetSummaryService {

    private final VehicleRepository vehicleRepository;
    private final DriverRepository driverRepository;
    private final WorkOrderRepository workOrderRepository;
    private final MaintenanceService maintenanceService;

    public FleetSummaryResponse summary(int dueSoonDays) {
        UUID orgId = CurrentUser.get().organisationId();

        long totalVehicles = vehicleRepository
                .countByOrganisationIdAndStatusAndDeletedAtIsNull(orgId, VehicleStatus.ACTIVE)
                + vehicleRepository.countByOrganisationIdAndStatusAndDeletedAtIsNull(orgId, VehicleStatus.IDLE)
                + vehicleRepository.countByOrganisationIdAndStatusAndDeletedAtIsNull(orgId, VehicleStatus.IN_SERVICE);

        long openWorkOrders = workOrderRepository
                .findByOrganisationIdAndStatusAndDeletedAtIsNull(orgId, WorkOrderStatus.OPEN).size()
                + workOrderRepository
                .findByOrganisationIdAndStatusAndDeletedAtIsNull(orgId, WorkOrderStatus.IN_PROGRESS).size();

        return new FleetSummaryResponse(
                totalVehicles,
                vehicleRepository.countByOrganisationIdAndStatusAndDeletedAtIsNull(orgId, VehicleStatus.ACTIVE),
                vehicleRepository.countByOrganisationIdAndStatusAndDeletedAtIsNull(orgId, VehicleStatus.IDLE),
                vehicleRepository.countByOrganisationIdAndStatusAndDeletedAtIsNull(orgId, VehicleStatus.IN_SERVICE),
                vehicleRepository.countByOrganisationIdAndStatusAndDeletedAtIsNull(orgId, VehicleStatus.RETIRED),
                driverRepository.countByOrganisationIdAndStatusAndDeletedAtIsNull(orgId, DriverStatus.ACTIVE)
                        + driverRepository.countByOrganisationIdAndStatusAndDeletedAtIsNull(orgId, DriverStatus.ON_TRIP)
                        + driverRepository.countByOrganisationIdAndStatusAndDeletedAtIsNull(orgId, DriverStatus.INACTIVE),
                driverRepository.countByOrganisationIdAndStatusAndDeletedAtIsNull(orgId, DriverStatus.ACTIVE),
                driverRepository.countByOrganisationIdAndStatusAndDeletedAtIsNull(orgId, DriverStatus.ON_TRIP),
                openWorkOrders,
                maintenanceService.dueWithin(dueSoonDays).size());
    }
}
