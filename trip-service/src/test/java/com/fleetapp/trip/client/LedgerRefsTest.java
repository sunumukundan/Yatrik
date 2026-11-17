package com.fleetapp.trip.client;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * The idempotency keys trip-service sends to ledger-service.
 *
 * These values are pinned because changing them has a real cost: trips completed
 * before the change already have ledger entries under the old key. If the formula
 * changes, a retried completion posts a SECOND entry and the customer is charged
 * twice.
 */
class LedgerRefsTest {

    private static final UUID TRIP_ID = UUID.fromString("a5bb95eb-947e-4778-a9f4-67dc9ecadc1d");

    @Test
    void freightDebitKeyIsStable() {
        // Golden value. If this fails, someone changed the key and trips already
        // posted to the ledger could be charged a second time on retry.
        // Do not just update the value.
        assertEquals(
                UUID.fromString("5a405788-dabc-36ad-887c-cc4d0053589b"),
                LedgerRefs.freightDebit(TRIP_ID));
    }

    @Test
    void customerAdvanceKeyIsStable() {
        // Golden value, same reason as above: advancing a customer twice is a
        // real-money error.
        assertEquals(
                UUID.fromString("d978f28e-e3e5-3c2a-9891-a80b5096f580"),
                LedgerRefs.customerAdvanceCredit(TRIP_ID));
    }

    @Test
    void freightAndAdvanceUseDifferentKeys() {
        // If they shared a key, the advance entry would be rejected as a duplicate of
        // the freight entry and the customer's balance would never net out.
        assertNotEquals(
                LedgerRefs.freightDebit(TRIP_ID),
                LedgerRefs.customerAdvanceCredit(TRIP_ID));
    }

    @Test
    void theSameTripAlwaysProducesTheSameKey() {
        assertEquals(LedgerRefs.freightDebit(TRIP_ID), LedgerRefs.freightDebit(TRIP_ID));
    }

    @Test
    void differentTripsProduceDifferentKeys() {
        UUID other = UUID.fromString("00000000-0000-0000-0000-000000000001");
        assertNotEquals(LedgerRefs.freightDebit(TRIP_ID), LedgerRefs.freightDebit(other));
    }
}
