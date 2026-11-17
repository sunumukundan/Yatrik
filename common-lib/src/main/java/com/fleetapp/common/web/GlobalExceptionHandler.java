package com.fleetapp.common.web;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * One error model for every service: the body is always {timestamp, error}.
 *
 * Services that need extra behaviour (e.g. trip-service's "closed trip" conflict)
 * declare their own advice, and give it a HIGHER precedence than this one. The advice
 * beans are consulted in order and the first matching handler wins, so this one sits
 * last: it is the fallback for the generic cases, and a service that knows better
 * about a specific exception gets to say so.
 */
@RestControllerAdvice
@Order(Ordered.LOWEST_PRECEDENCE)
public class GlobalExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(EntityNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body(ex.getMessage()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleConflict(DataIntegrityViolationException ex) {
        // Covers the unique constraints (duplicate registration number, phone, client_ref).
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(body("Duplicate or conflicting record (possibly already submitted)"));
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<Map<String, Object>> handleStale(OptimisticLockingFailureException ex) {
        // Two requests modified the same row at once (e.g. a double-tapped submit).
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(body("Record was modified by another request; please reload and retry"));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .orElse("Validation failed");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body(message));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body(ex.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalState(IllegalStateException ex) {
        // Thrown by CurrentUser.get() when no authenticated user is in context.
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body(ex.getMessage()));
    }

    private Map<String, Object> body(String message) {
        Map<String, Object> map = new HashMap<>();
        map.put("timestamp", Instant.now().toString());
        map.put("error", message);
        return map;
    }
}
