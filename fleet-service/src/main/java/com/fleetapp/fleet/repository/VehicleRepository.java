package com.fleetapp.fleet.repository;

import com.fleetapp.fleet.domain.Vehicle;
import com.fleetapp.fleet.domain.VehicleStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VehicleRepository extends JpaRepository<Vehicle, UUID> {

    List<Vehicle> findByOrganisationIdAndDeletedAtIsNull(UUID organisationId);

    Optional<Vehicle> findByIdAndOrganisationIdAndDeletedAtIsNull(UUID id, UUID organisationId);

    /**
     * Counts vehicles in one status, for the fleet summary. Scoped by organisation so
     * one tenant's numbers never include another's.
     */
    long countByOrganisationIdAndStatusAndDeletedAtIsNull(UUID organisationId, VehicleStatus status);

    List<Vehicle> findByOrganisationIdAndStatusAndDeletedAtIsNull(UUID organisationId, VehicleStatus status);
}
