package com.fleetapp.trip.exception;

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
 * Trip-service's own error model.
 *
 * The shared one in common-lib covers the generic cases; this one adds the trip-specific
 * meanings. Spring picks the most specific handler, so the trip-specific ones win.
 *
 * Named distinctly from the shared GlobalExceptionHandler on purpose: two
 * @RestControllerAdvice beans with the same simple class name both want the bean name
 * "globalExceptionHandler", and Spring refuses to start when two beans claim one name.
 *
 * @Order puts this ahead of the shared advice. The advice beans are consulted in
 * order and the first matching handler wins, so the trip-specific answers must be
 * reached before the generic ones.
 */
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TripExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(EntityNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body(ex.getMessage()));
    }

    @ExceptionHandler(InvalidTripException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidTrip(InvalidTripException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body(ex.getMessage()));
    }

    @ExceptionHandler(TripClosedException.class)
    public ResponseEntity<Map<String, Object>> handleClosed(TripClosedException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body(ex.getMessage()));
    }

    @ExceptionHandler(LedgerSyncException.class)
    public ResponseEntity<Map<String, Object>> handleLedgerSync(LedgerSyncException ex) {
        // 502 = ledger unreachable/erroring (retry later); 422 = customer not in ledger.
        return ResponseEntity.status(ex.getStatus()).body(body(ex.getMessage()));
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<Map<String, Object>> handleStale(OptimisticLockingFailureException ex) {
        // Two requests modified the same trip at once (e.g. a double-tapped "Complete").
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(body("Trip was modified by another request; please reload and retry"));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleConflict(DataIntegrityViolationException ex) {
        // Covers the client_ref uniqueness violation: an offline-synced expense
        // being retried. 409 tells the mobile client "already saved, don't resubmit"
        // rather than a generic 500.
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(body("Duplicate or conflicting record (possibly already submitted)"));
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
        // Thrown by CurrentUser.get() when no authenticated user is in context.
        // TripClosedException is intentionally a separate type (see its own javadoc)
        // so a closed-trip conflict is never misreported as 401 here.
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body(ex.getMessage()));
    }

    private Map<String, Object> body(String message) {
        Map<String, Object> map = new HashMap<>();
        map.put("timestamp", Instant.now().toString());
        map.put("error", message);
        return map;
    }
}
