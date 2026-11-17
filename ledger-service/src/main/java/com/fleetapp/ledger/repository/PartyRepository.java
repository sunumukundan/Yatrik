package com.fleetapp.ledger.repository;

import com.fleetapp.ledger.domain.Party;
import com.fleetapp.ledger.domain.PartyType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PartyRepository extends JpaRepository<Party, UUID> {

    List<Party> findByOrganisationIdAndDeletedAtIsNull(UUID organisationId);

    List<Party> findByOrganisationIdAndTypeAndDeletedAtIsNull(UUID organisationId, PartyType type);

    Optional<Party> findByIdAndOrganisationIdAndDeletedAtIsNull(UUID id, UUID organisationId);
}
