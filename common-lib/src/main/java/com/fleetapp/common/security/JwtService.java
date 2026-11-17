package com.fleetapp.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

/**
 * Verifies and reads access tokens issued by identity-service. A service that uses
 * this never issues tokens itself — it only trusts ones signed with the same shared
 * secret.
 *
 * For the MVP this is a shared symmetric key via config (app.jwt.secret, identical
 * value in every service). Move to asymmetric signing (identity-service holds the
 * private key, other services verify with a public key / JWKS endpoint) before this
 * secret needs to be distributed beyond a handful of trusted services.
 *
 * Deliberately NOT a Spring bean: identity-service needs a token *issuer* with the
 * same name, and two beans of one name in one context is a startup failure. Services
 * get this class as a bean from CommonSecurityConfig instead.
 */
public class JwtService {

    private final SecretKey key;

    public JwtService(String secret) {
        // The secret must be at least 256 bits (32 chars) for HS256.
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
