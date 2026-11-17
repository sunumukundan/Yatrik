package com.fleetapp.identity.security;

import com.fleetapp.identity.security.RefreshTokenPolicy.Verdict;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The refresh-token acceptance rules.
 *
 * These are the rules that decide whether a driver gets logged out mid-route because
 * their phone lost the network, and equally whether a stolen token stays useful. The
 * boundaries are tested here because they are exactly the cases that are easy to get
 * subtly wrong.
 */
class RefreshTokenPolicyTest {

    private static final Instant NOW = Instant.parse("2026-01-15T10:00:00Z");

    @Test
    void acceptsAFreshToken() {
        assertEquals(Verdict.ACCEPT, RefreshTokenPolicy.evaluate(
                NOW, NOW.plusSeconds(3600), null, null));
    }

    @Test
    void rejectsAnExpiredToken() {
        assertEquals(Verdict.EXPIRED, RefreshTokenPolicy.evaluate(
                NOW, NOW.minusSeconds(1), null, null));
    }

    @Test
    void rejectsATokenThatExpiresExactlyNow() {
        // The expiry is exclusive: a token is not valid at the instant it expires.
        assertEquals(Verdict.EXPIRED, RefreshTokenPolicy.evaluate(
                NOW, NOW, null, null));
    }

    @Test
    void rejectsARevokedToken() {
        assertEquals(Verdict.REVOKED, RefreshTokenPolicy.evaluate(
                NOW, NOW.plusSeconds(3600), NOW.minusSeconds(10), null));
    }

    @Test
    void revocationBeatsTheGraceWindow() {
        // A token that was rotated out AND then logged out must not be honoured by the
        // grace window: logout has to win, or a driver could not really sign out.
        assertEquals(Verdict.REVOKED, RefreshTokenPolicy.evaluate(
                NOW, NOW.plusSeconds(3600), NOW.minusSeconds(10), NOW.minusSeconds(20)));
    }

    @Test
    void acceptsARotatedTokenInsideTheGraceWindow() {
        // The phone retried with the old token because it never saw the response.
        assertEquals(Verdict.ACCEPT, RefreshTokenPolicy.evaluate(
                NOW, NOW.plusSeconds(3600), null, NOW.minusSeconds(30)));
    }

    @Test
    void rejectsARotatedTokenAfterTheGraceWindow() {
        assertEquals(Verdict.REUSED_AFTER_GRACE, RefreshTokenPolicy.evaluate(
                NOW, NOW.plusSeconds(3600), null, NOW.minus(RefreshTokenPolicy.REUSE_GRACE).minusSeconds(1)));
    }

    @Test
    void acceptsARotatedTokenExactlyAtTheGraceBoundary() {
        // The window is inclusive at the boundary: exactly 2 minutes old is still fine.
        assertEquals(Verdict.ACCEPT, RefreshTokenPolicy.evaluate(
                NOW, NOW.plusSeconds(3600), null, NOW.minus(RefreshTokenPolicy.REUSE_GRACE)));
    }

    @Test
    void expiryBeatsTheGraceWindow() {
        // An expired token that was also rotated out is expired, not merely reused.
        assertEquals(Verdict.EXPIRED, RefreshTokenPolicy.evaluate(
                NOW, NOW.minusSeconds(1), null, NOW.minusSeconds(30)));
    }

    @Test
    void graceWindowIsTwoMinutes() {
        assertEquals(Duration.ofMinutes(2), RefreshTokenPolicy.REUSE_GRACE);
    }
}
