package com.fleetapp.trip.web.dto;

import com.fleetapp.trip.domain.Trip;
import com.fleetapp.trip.domain.TripStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record TripResponse(
        UUID id, UUID vehicleId, UUID driverId, UUID partyId,
        String origin, String destination, LocalDate startDate, LocalDate endDate,
        BigDecimal distanceKm, long freightAmount, long customerAdvance, long driverAdvance,
        TripStatus status, String notes, Instant closedAt
) {
    public static TripResponse from(Trip t) {
        return new TripResponse(t.getId(), t.getVehicleId(), t.getDriverId(), t.getPartyId(),
                t.getOrigin(), t.getDestination(), t.getStartDate(), t.getEndDate(),
                t.getDistanceKm(), t.getFreightAmount(), t.getCustomerAdvance(), t.getDriverAdvance(),
                t.getStatus(), t.getNotes(), t.getClosedAt());
    }
}
