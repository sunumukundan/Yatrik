package com.fleetapp.identity.exception;

/**
 * Thrown when email/password authentication fails.
 * Returns 401 to properly signal authentication failure to the client.
 */
public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException() {
        super("Invalid email or password");
    }
}