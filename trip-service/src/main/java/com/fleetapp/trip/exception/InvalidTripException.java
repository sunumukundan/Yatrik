package com.fleetapp.trip.exception;

/** The request describes a trip that cannot be recorded consistently. Maps to HTTP 400. */
public class InvalidTripException extends RuntimeException {
    public InvalidTripException(String message) {
        super(message);
    }
}
