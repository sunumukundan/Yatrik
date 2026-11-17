package com.fleetapp.fleet.repository;

import com.fleetapp.fleet.domain.VehicleDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface VehicleDocumentRepository extends JpaRepository<VehicleDocument, UUID> {

    List<VehicleDocument> findByVehicleIdAndDeletedAtIsNull(UUID vehicleId);

    // Powers the expiry-reminder job: documents expiring within the window, not yet reminded today.
    List<VehicleDocument> findByOrganisationIdAndExpiryDateBetweenAndDeletedAtIsNull(
            UUID organisationId, LocalDate from, LocalDate to);
}
