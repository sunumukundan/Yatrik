package com.fleetapp.trip.repository;

import com.fleetapp.trip.domain.Trip;
import com.fleetapp.trip.domain.TripStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TripRepository extends JpaRepository<Trip, UUID> {

    List<Trip> findByOrganisationIdAndDeletedAtIsNullOrderByCreatedAtDesc(UUID organisationId);

    Optional<Trip> findByIdAndOrganisationIdAndDeletedAtIsNull(UUID id, UUID organisationId);

    List<Trip> findByOrganisationIdAndStatusAndDeletedAtIsNull(UUID organisationId, TripStatus status);

    /**
     * The trips that started inside a window — the unit the reporting service
     * aggregates over.
     */
    List<Trip> findByOrganisationIdAndStartDateBetweenAndDeletedAtIsNull(
            UUID organisationId, LocalDate from, LocalDate to);
}
