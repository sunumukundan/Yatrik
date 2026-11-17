package com.fleetapp.fleet.repository;

import com.fleetapp.fleet.domain.DriverDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface DriverDocumentRepository extends JpaRepository<DriverDocument, UUID> {

    List<DriverDocument> findByDriverIdAndDeletedAtIsNull(UUID driverId);

    List<DriverDocument> findByOrganisationIdAndExpiryDateBetweenAndDeletedAtIsNull(
            UUID organisationId, LocalDate from, LocalDate to);
}
