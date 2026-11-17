package com.fleetapp.identity.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

/**
 * Issues and parses access tokens — the only service in the platform that does.
 *
 * Keep ACCESS_TOKEN_MINUTES short (15–30 min): there is no revocation list for
 * access tokens, so the only way to end a session early is to let the token expire
 * and stop using the refresh token. Only refresh tokens are revocable.
 *
 * The token contract must stay identical across the platform:
 *   HS256, subject = user id, claim "org" = organisation id, claim "role",
 *   30-minute expiry, symmetric shared secret.
 *
 * Milestone 1 additions:
 *   claim "fleet" = assigned fleet id (for FLEET_OWNER)
 *   claim "license_active" = whether user has active license seat
 *   claim "org_status" = organisation status
 */
@Service
public class JwtService {

    private final SecretKey key;
    private final long accessTokenMinutes;

    public JwtService(@Value("${app.jwt.secret}") String secret,
                      @Value("${app.jwt.access-token-minutes:30}") long accessTokenMinutes) {
        // The secret must be at least 256 bits (32 chars) for HS256.
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenMinutes = accessTokenMinutes;
    }

    public String generateAccessToken(UUID userId, UUID organisationId, String role, UUID createdBy, UUID assignedFleetId, Boolean licenseSeatActive, String orgStatus) {
        Instant now = Instant.now();
        var builder = Jwts.builder()
                .subject(userId.toString())
                .claim("org", organisationId.toString())
                .claim("role", role)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(accessTokenMinutes, ChronoUnit.MINUTES)))
                .claim("created_by", createdBy != null ? createdBy.toString() : userId.toString())
                .claim("iat", now.getEpochSecond());

        if (assignedFleetId != null) {
            builder.claim("fleet", assignedFleetId.toString());
        }
        if (licenseSeatActive != null) {
            builder.claim("license_active", licenseSeatActive);
        }
        if (orgStatus != null) {
            builder.claim("org_status", orgStatus);
        }

        return builder.signWith(key).compact();
    }

    public String generateAccessToken(UUID userId, UUID organisationId, String role) {
        return generateAccessToken(userId, organisationId, role, null, null, null, null);
    }

    public String generateAccessToken(UUID userId, UUID organisationId, String role, UUID createdBy) {
        return generateAccessToken(userId, organisationId, role, createdBy, null, null, null);
    }

    public Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public long accessTokenValiditySeconds() {
        return accessTokenMinutes * 60;
    }
}
