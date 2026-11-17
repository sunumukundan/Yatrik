package com.fleetapp.identity.exception;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Identity-service's error model.
 *
 * Named distinctly from the shared GlobalExceptionHandler in common-lib on purpose:
 * two @RestControllerAdvice beans with the same simple class name both want the bean
 * name "globalExceptionHandler", and Spring refuses to start when two beans claim the
 * same name. The class name is what the bean name is derived from.
 *
 * @Order gives it a defined place in the advice chain. Spring consults the advice
 * beans in order and takes the first handler that matches, so an explicit order is
 * what makes "the more specific handler wins" a fact rather than a hope.
 */
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class IdentityExceptionHandler {

    @ExceptionHandler(InvalidRefreshTokenException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidRefresh(InvalidRefreshTokenException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body(ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
        // e.g. "Invalid or expired OTP" from AuthService.
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body(ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .orElse("Validation failed");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body(message));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalState(IllegalStateException ex) {
        // e.g. the SMS provider failed to deliver the OTP. The user simply never received a
        // code, so this is not an authentication failure — tell them to request a new one
        // rather than leaking an "unauthorized" signal mid-sign-in.
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(body("Failed to send the verification code. Please request a new one."));
    }

    private Map<String, Object> body(String message) {
        Map<String, Object> map = new HashMap<>();
        map.put("timestamp", Instant.now().toString());
        map.put("error", message);
        return map;
    }
}
