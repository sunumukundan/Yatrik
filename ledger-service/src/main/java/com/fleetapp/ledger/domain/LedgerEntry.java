package com.fleetapp.ledger.domain;

import com.fleetapp.common.domain.TenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "ledger_entry", uniqueConstraints = {
        @UniqueConstraint(name = "uq_ledger_client_ref", columnNames = {"organisation_id", "client_ref"})
})
@Getter
@Setter
@NoArgsConstructor
public class LedgerEntry extends TenantEntity {

    @Id
    @JdbcTypeCode(SqlTypes.UUID)
    @GeneratedValue
    private UUID id;

    @Column(name = "party_id", nullable = false)
    private UUID partyId;

    /** Nullable: not every entry originates from a trip (e.g. a manual cash receipt). */
    @Column(name = "trip_id")
    private UUID tripId;

    @Enumerated(EnumType.STRING)
    @Column(name = "entry_type", nullable = false)
    private EntryType entryType;

    @Column(nullable = false)
    private long amount; // paise, always positive — direction comes from entryType

    @Column(name = "entry_date", nullable = false)
    private LocalDate entryDate = LocalDate.now();

    @Enumerated(EnumType.STRING)
    private PaymentMode mode;

    @Column(length = 100)
    private String reference;

    @Column(columnDefinition = "TEXT")
    private String note;

    /**
     * Idempotency key for offline-synced entries, same pattern as TripExpense.
     * Nullable here (unlike trip_expense) since a manually-entered ledger row from
     * the owner's web dashboard has no offline-sync concern.
     */
    @Column(name = "client_ref")
    private UUID clientRef;
}
