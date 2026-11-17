package com.fleetapp.common.domain;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

/**
 * The audit columns every table in this platform carries.
 *
 * The id column is deliberately NOT here: it is declared per entity because the
 * primary-key strategy differs (entities use a generated UUID, the ledger's cached
 * balance uses the party id itself as its key).
 */
@MappedSuperclass
@Getter
@Setter
public abstract class AuditedEntity {

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "created_by")
    private UUID createdBy;

    /**
     * Soft delete. A row with a deleted_at is invisible to every query in this
     * codebase, but stays in the table so the history behind a financial figure
     * remains auditable.
     */
    @Column(name = "deleted_at")
    private Instant deletedAt;

    /**
     * Optimistic locking. Two concurrent writers cannot both win: the loser gets an
     * OptimisticLockingFailureException, which the error model maps to 409.
     */
    @Version
    @Column(nullable = false)
    private int version;
}
