package com.fleetapp.fleet.repository;

import com.fleetapp.fleet.domain.Driver;
import com.fleetapp.fleet.domain.DriverStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DriverRepository extends JpaRepository<Driver, UUID> {

    List<Driver> findByOrganisationIdAndDeletedAtIsNull(UUID organisationId);

    Optional<Driver> findByIdAndOrganisationIdAndDeletedAtIsNull(UUID id, UUID organisationId);

    long countByOrganisationIdAndStatusAndDeletedAtIsNull(UUID organisationId, DriverStatus status);
}
