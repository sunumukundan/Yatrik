package com.fleetapp.identity.service;

import com.fleetapp.identity.domain.Organisation;
import com.fleetapp.identity.domain.OrgStatus;
import com.fleetapp.identity.domain.SubscriptionPlan;
import com.fleetapp.identity.repository.OrganisationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class OrganisationService {

    private final OrganisationRepository organisationRepository;

    public Organisation createOrganisation(Organisation organisation) {
        return organisationRepository.save(organisation);
    }

    public Organisation getOrganisation(UUID id) {
        return organisationRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Organisation not found: " + id));
    }

    public Organisation updateOrganisation(UUID id, Organisation organisation) {
        Organisation existing = getOrganisation(id);
        // Update existing fields from the provided organisation
        existing.setName(organisation.getName());
        existing.setGstNo(organisation.getGstNo());
        existing.setStatus(organisation.getStatus());
        existing.setPlan(organisation.getPlan());
        existing.setDefaultLanguage(organisation.getDefaultLanguage());
        return organisationRepository.save(existing);
    }

    public void deleteOrganisation(UUID id) {
        Organisation organisation = getOrganisation(id);
        organisationRepository.delete(organisation);
    }

    public List<Organisation> getOrganisationsByStatus(OrgStatus status) {
        return organisationRepository.findByStatus(status);
    }

    public List<Organisation> getOrganisationsByPlan(SubscriptionPlan plan) {
        return organisationRepository.findByPlan(plan);
    }

    public List<Organisation> getAllOrganisations() {
        return organisationRepository.findAll();
    }
}