package com.fleetapp.fleet.exception;

/**
 * The request describes maintenance work that cannot be recorded consistently.
 * Maps to HTTP 400.
 */
public class InvalidMaintenanceException extends RuntimeException {
    public InvalidMaintenanceException(String message) {
        super(message);
    }
}
