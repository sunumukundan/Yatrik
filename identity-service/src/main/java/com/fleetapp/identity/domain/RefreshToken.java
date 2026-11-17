package com.fleetapp.identity.domain;

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
 * A refresh token, stored as a SHA-256 hash. The raw token exists only in the
 * response that issued it — the database never sees it, so a database dump cannot be
 * replayed against the API.
 */
@Entity
@Table(name = "refresh_token")
@Getter
@Setter
@NoArgsConstructor
public class RefreshToken {

    @Id
    @JdbcTypeCode(SqlTypes.UUID)
    @GeneratedValue
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "token_hash", nullable = false, length = 255, unique = true)
    private String tokenHash;

    @Column(name = "device_id", length = 100)
    private String deviceId;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    /** Set on logout. Never honoured, not even inside the reuse grace window. */
    @Column(name = "revoked_at")
    private Instant revokedAt;

    /**
     * Set when the token is exchanged for a new one. Distinct from revokedAt: a
     * replaced token stays usable for a short grace window, so a lost response on a
     * flaky mobile network does not log the driver out mid-route.
     */
    @Column(name = "replaced_at")
    private Instant replacedAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;
}
