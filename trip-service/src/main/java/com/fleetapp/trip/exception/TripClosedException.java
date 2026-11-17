package com.fleetapp.trip.exception;

/**
 * Thrown when an operation tries to modify a trip after it has been closed
 * (status COMPLETED, closedAt set). Deliberately a distinct type from
 * IllegalStateException — CurrentUser.get() also throws IllegalStateException for
 * "no authenticated user", and conflating the two would make a closed-trip conflict
 * (409) indistinguishable from a missing-auth error (401) in GlobalExceptionHandler.
 */
public class TripClosedException extends RuntimeException {
    public TripClosedException(String message) {
        super(message);
    }
}
