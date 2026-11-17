package com.fleetapp.identity.security;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * Deterministic hashing for opaque tokens that must be looked up by hash
 * (refresh tokens). Do NOT use BCrypt here — BCrypt is salted and non-deterministic,
 * so the same input produces a different hash every call, which makes a
 * "find by hash" query impossible. BCrypt is correct for OTPs/passwords because
 * those are only ever verified (matches()), never searched for.
 */
@Component
public class TokenHasher {

    public String sha256(String raw) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(raw.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
