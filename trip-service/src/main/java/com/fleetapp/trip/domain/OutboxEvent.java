package com.fleetapp.trip.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

/**
 * A domain event written in the same transaction as the change that caused it.
 *
 * The outbox pattern: the row is committed together with the business data, so an
 * event can never be lost because the broker was down, and can never exist for a
 * change that was rolled back. A separate publisher picks up the unpublished rows
 * and forwards them to the broker, then stamps published_at.
 *
 * Nothing publishes these yet — the MVP calls ledger-service directly. The table is
 * here so the migration to a broker does not need a schema change.
 *
 * Deliberately not extending AuditedEntity: an outbox row is never edited, never
 * soft-deleted and never versioned, so it carries only the columns it actually uses.
 */
@Entity
@Table(name = "outbox_event")
@Getter
@Setter
@NoArgsConstructor
public class OutboxEvent {

    @Id
    @JdbcTypeCode(SqlTypes.UUID)
    @GeneratedValue
    private UUID id;

    /** e.g. 'TRIP' */
    @Column(name = "aggregate_type", nullable = false, length = 50)
    private String aggregateType;

    @Column(name = "aggregate_id", nullable = false)
    private UUID aggregateId;

    /** e.g. 'TripCompleted' */
    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;

    @Column(nullable = false, columnDefinition = "jsonb")
    private String payload;

    @Column(name = "organisation_id", nullable = false)
    private UUID organisationId;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @Column(name = "published_at")
    private Instant publishedAt;
}
