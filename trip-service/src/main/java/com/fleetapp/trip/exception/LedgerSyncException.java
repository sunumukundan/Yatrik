package com.fleetapp.trip.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when trip-service cannot record a trip's freight in ledger-service.
 * Carries the HTTP status to return, because the right answer differs by cause:
 * 502 when the ledger is unreachable or erroring (retry later), 422 when the trip's
 * customer doesn't exist in the ledger (retrying won't help; fix the data).
 */
public class LedgerSyncException extends RuntimeException {

    private final HttpStatus status;

    public LedgerSyncException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
