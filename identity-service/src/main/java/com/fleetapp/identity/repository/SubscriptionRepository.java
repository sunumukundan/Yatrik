package com.fleetapp.identity.repository;

import com.fleetapp.identity.domain.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {

    List<Subscription> findByOrganisationIdAndDeletedAtIsNullOrderByValidTillDesc(UUID organisationId);
}
