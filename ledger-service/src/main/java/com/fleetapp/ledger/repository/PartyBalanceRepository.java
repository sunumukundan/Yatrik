package com.fleetapp.ledger.repository;

import com.fleetapp.ledger.domain.PartyBalance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PartyBalanceRepository extends JpaRepository<PartyBalance, UUID> {

    List<PartyBalance> findByOrganisationId(UUID organisationId);
}
