package com.fleetapp.common.security;

import java.util.UUID;

/**
 * Per-request identity extracted from the JWT by {@link JwtAuthFilter}. Controllers and
 * services read organisationId from here — NEVER from a client-supplied path or body
 * field — so a caller can never query or write another organisation's data by passing
 * a different id.
 *
 * Backed by a ThreadLocal, so it must be cleared at the end of every request: the
 * servlet container pools threads, and a value left behind would leak into the next
 * request that lands on the same thread. JwtAuthFilter clears it in a finally block.
 */
public final class CurrentUser {

    private static final ThreadLocal<CurrentUser> HOLDER = new ThreadLocal<>();

    private final UUID userId;
    private final UUID organisationId;
    private final String role;
    private final String token;

    private CurrentUser(UUID userId, UUID organisationId, String role, String token) {
        this.userId = userId;
        this.organisationId = organisationId;
        this.role = role;
        this.token = token;
    }

    public static void set(UUID userId, UUID organisationId, String role, String token) {
        HOLDER.set(new CurrentUser(userId, organisationId, role, token));
    }

    public static CurrentUser get() {
        CurrentUser current = HOLDER.get();
        if (current == null) {
            throw new IllegalStateException("No authenticated user in context");
        }
        return current;
    }

    public static void clear() {
        HOLDER.remove();
    }

    public UUID userId() { return userId; }
    public UUID organisationId() { return organisationId; }
    public String role() { return role; }

    /**
     * The caller's raw JWT, kept only so a service can forward it on calls to other
     * services (e.g. trip-service calling ledger-service). Never log this value.
     */
    public String token() { return token; }
}
