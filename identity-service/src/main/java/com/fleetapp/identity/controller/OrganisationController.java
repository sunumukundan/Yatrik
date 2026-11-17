package com.fleetapp.identity.controller;

import com.fleetapp.identity.domain.Organisation;
import com.fleetapp.identity.domain.OrgStatus;
import com.fleetapp.identity.domain.SubscriptionPlan;
import com.fleetapp.identity.service.OrganisationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/organisations")
@RequiredArgsConstructor
public class OrganisationController {

    private final OrganisationService organisationService;

    @PostMapping("/organisations")
    @ResponseStatus(HttpStatus.CREATED)
    public Organisation createOrganisation(@Valid @RequestBody Organisation organisation) {
        return organisationService.createOrganisation(organisation);
    }

    @GetMapping("/organisations/{id}")
    public Organisation getOrganisation(@PathVariable UUID id) {
        return organisationService.getOrganisation(id);
    }

    @PutMapping("/organisations/{id}")
    public Organisation updateOrganisation(@PathVariable UUID id, @Valid @RequestBody Organisation organisation) {
        return organisationService.updateOrganisation(id, organisation);
    }

    @DeleteMapping("/organisations/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteOrganisation(@PathVariable UUID id) {
        organisationService.deleteOrganisation(id);
    }

    @GetMapping("/organisations/status/{status}")
    public List<Organisation> getOrganisationsByStatus(@PathVariable OrgStatus status) {
        return organisationService.getOrganisationsByStatus(status);
    }

    @GetMapping("/organisations/plan/{plan}")
    public List<Organisation> getOrganisationsByPlan(@PathVariable SubscriptionPlan plan) {
        return organisationService.getOrganisationsByPlan(plan);
    }

    @GetMapping("/organisations")
    public List<Organisation> getAllOrganisations() {
        return organisationService.getAllOrganisations();
    }
}