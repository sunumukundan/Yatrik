package com.fleetapp.common.domain;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/**
 * The columns every tenant-scoped table in this platform carries.
 *
 * Keeping them in one place means a new entity cannot accidentally ship without a
 * created_at, a version, or the organisation_id that scopes it — and the query
 * conventions (findByOrganisationIdAndDeletedAtIsNull) hold across services.
 *
 * A table that is NOT scoped to a tenant (the organisation itself, the OTP request,
 * the refresh token) extends {@link AuditedEntity} instead.
 */
@MappedSuperclass
@Getter
@Setter
public abstract class TenantEntity extends AuditedEntity {

    /**
     * The tenant this row belongs to. Read from the JWT, never from the request
     * body, so a caller cannot reach another organisation's data.
     */
    @Column(name = "organisation_id", nullable = false, updatable = false)
    private UUID organisationId;
}
