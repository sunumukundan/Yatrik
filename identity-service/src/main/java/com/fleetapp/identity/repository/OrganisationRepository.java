package com.fleetapp.identity.repository;

import com.fleetapp.identity.domain.Organisation;
import com.fleetapp.identity.domain.OrgStatus;
import com.fleetapp.identity.domain.SubscriptionPlan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OrganisationRepository extends JpaRepository<Organisation, UUID> {

    List<Organisation> findByStatus(OrgStatus status);

    List<Organisation> findByPlan(SubscriptionPlan plan);
}
