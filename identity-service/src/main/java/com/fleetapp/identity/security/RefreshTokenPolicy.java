package com.fleetapp.identity.security;

import java.time.Duration;
import java.time.Instant;

/**
 * The accept/reject rule for presented refresh tokens, kept as a pure function (no
 * Spring, no database, no clock) so it can be unit tested exhaustively. AuthService
 * loads the token row and calls this with "now" passed in.
 *
 * Rotation + grace window: every successful refresh issues a NEW refresh token and
 * stamps the old one with replacedAt. The old one stays usable for REUSE_GRACE after
 * that. Why: mobile networks drop responses. If the server rotated a token but the phone
 * never received the new one, the phone retries with the old token; a strict rule would
 * log a driver out mid-route and force a fresh SMS OTP. After the grace window an old
 * token is rejected, which is what limits the value of a stolen one.
 *
 * revokedAt is different from replacedAt: it means explicitly killed (logout) and is
 * never honoured, grace or not.
 */
public final class RefreshTokenPolicy {

    public static final Duration REUSE_GRACE = Duration.ofMinutes(2);

    public enum Verdict { ACCEPT, REVOKED, EXPIRED, REUSED_AFTER_GRACE }

    private RefreshTokenPolicy() {}

    public static Verdict evaluate(Instant now, Instant expiresAt, Instant revokedAt, Instant replacedAt) {
        if (revokedAt != null) {
            return Verdict.REVOKED;
        }
        if (!now.isBefore(expiresAt)) {
            return Verdict.EXPIRED;
        }
        if (replacedAt != null && now.isAfter(replacedAt.plus(REUSE_GRACE))) {
            return Verdict.REUSED_AFTER_GRACE;
        }
        return Verdict.ACCEPT;
    }
}
