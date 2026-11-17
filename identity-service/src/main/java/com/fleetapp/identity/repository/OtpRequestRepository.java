package com.fleetapp.identity.repository;

import com.fleetapp.identity.domain.OtpRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface OtpRequestRepository extends JpaRepository<OtpRequest, UUID> {

    Optional<OtpRequest> findFirstByIdentifierAndConsumedAtIsNullAndExpiresAtAfterOrderByCreatedAtDesc(
            String identifier, Instant now);
}
