package com.fleetapp.trip.client;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * The idempotency key (clientRef) for each ledger entry trip-service posts. Derived from
 * the trip id, so retrying a half-finished completion re-sends the SAME key and
 * ledger-service's unique (organisation_id, client_ref) constraint turns the repeat into a
 * harmless 409 instead of a second charge.
 *
 * Pure and dependency-free on purpose, so it can be unit tested without Spring.
 *
 * Two rules, both enforced by LedgerRefsTest:
 *  - freightDebit's formula must NEVER change: trips completed before the advance split
 *    already have ledger entries under this key.
 *  - every entry type needs its own prefix, or a trip's freight and advance entries would
 *    share a key and the second would be rejected as a duplicate.
 */
public final class LedgerRefs {

    private LedgerRefs() {}

    public static UUID freightDebit(UUID tripId) {
        return derive("trip-freight:", tripId);
    }

    public static UUID customerAdvanceCredit(UUID tripId) {
        return derive("trip-customer-advance:", tripId);
    }

    private static UUID derive(String prefix, UUID tripId) {
        return UUID.nameUUIDFromBytes((prefix + tripId).getBytes(StandardCharsets.UTF_8));
    }
}
