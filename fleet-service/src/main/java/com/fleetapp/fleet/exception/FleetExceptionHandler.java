package com.fleetapp.fleet.exception;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Fleet-service's own error model.
 *
 * Sits ahead of the shared handler in common-lib (see the @Order note there): these
 * are the cases where the fleet service knows better than the generic advice what the
 * status code should be.
 */
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class FleetExceptionHandler {

    /**
     * A maintenance request that cannot be recorded consistently — servicing a service
     * twice, raising a work order for a vehicle that does not exist, and so on. That is
     * the caller's mistake, so 400, not 500.
     */
    @ExceptionHandler(InvalidMaintenanceException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidMaintenance(InvalidMaintenanceException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body(ex.getMessage()));
    }

    private Map<String, Object> body(String message) {
        Map<String, Object> map = new HashMap<>();
        map.put("timestamp", Instant.now().toString());
        map.put("error", message);
        return map;
    }
}
