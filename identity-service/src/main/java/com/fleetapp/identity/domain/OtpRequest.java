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
 * A one-time password sent to a phone number or email.
 *
 * Not tenant-scoped: the OTP is requested before anyone is authenticated, so there is
 * no organisation to scope it to. The identifier (phone or email) is the only identifying field.
 */
@Entity
@Table(name = "otp_request")
@Getter
@Setter
@NoArgsConstructor
public class OtpRequest {

    @Id
    @JdbcTypeCode(SqlTypes.UUID)
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, length = 255)
    private String identifier;

    @Column(nullable = false, length = 20)
    private String identifierType; // "phone" or "email"

    /** BCrypt hash of the OTP. Never the OTP itself. */
    @Column(name = "otp_hash", nullable = false, length = 255)
    private String otpHash;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(nullable = false)
    private int attempts = 0;

    /** Set once the OTP has been used successfully. */
    @Column(name = "consumed_at")
    private Instant consumedAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;
}
