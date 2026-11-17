package com.fleetapp.fleet.repository;

import com.fleetapp.fleet.domain.MaintenanceRecord;
import com.fleetapp.fleet.domain.ServiceScheduleStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface MaintenanceRecordRepository extends JpaRepository<MaintenanceRecord, UUID> {

    List<MaintenanceRecord> findByVehicleIdAndDeletedAtIsNullOrderByDueDateAsc(UUID vehicleId);

    List<MaintenanceRecord> findByOrganisationIdAndDeletedAtIsNullOrderByDueDateAsc(UUID organisationId);

    /**
     * Services falling due inside the window — used by the "due soon" report and by
     * the reminder job.
     */
    List<MaintenanceRecord> findByOrganisationIdAndStatusAndDueDateBetweenAndDeletedAtIsNull(
            UUID organisationId, ServiceScheduleStatus status, LocalDate from, LocalDate to);

    List<MaintenanceRecord> findByOrganisationIdAndStatusAndDeletedAtIsNull(
            UUID organisationId, ServiceScheduleStatus status);
}
