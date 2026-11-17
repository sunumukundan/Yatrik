package com.fleetapp.identity.exception;

/**
 * Thrown for every kind of rejected refresh token.
 *
 * One fixed message on purpose: telling a caller WHICH part of the token was wrong
 * would help someone probing for a valid token hash. The reason is logged server-side
 * instead.
 */
public class InvalidRefreshTokenException extends RuntimeException {

    public InvalidRefreshTokenException() {
        super("Invalid or expired refresh token");
    }
}
