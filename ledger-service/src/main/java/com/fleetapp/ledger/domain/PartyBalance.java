package com.fleetapp.ledger.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

/**
 * Cached read model, NOT a source of truth — rebuildable at any time by summing
 * ledger_entry for a party (debits positive, credits negative). No version/soft-delete
 * columns, matching the DB schema exactly: this row is overwritten in place, never
 * versioned or soft-deleted, because it carries no information ledger_entry doesn't
 * already have.
 */
@Entity
@Table(name = "party_balance")
@Getter
@Setter
@NoArgsConstructor
public class PartyBalance {

    @Id
    @Column(name = "party_id")
    @JdbcTypeCode(SqlTypes.UUID)
    private UUID partyId;

    @Column(name = "organisation_id", nullable = false)
    private UUID organisationId;

    @Column(nullable = false)
    private long balance = 0; // paise, +ve = they owe us

    @Column(name = "last_entry_at")
    private Instant lastEntryAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;
}
