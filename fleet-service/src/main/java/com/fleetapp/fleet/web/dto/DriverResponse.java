package com.fleetapp.fleet.web.dto;

import com.fleetapp.fleet.domain.Driver;
import com.fleetapp.fleet.domain.DriverStatus;

import java.time.LocalDate;
import java.util.UUID;

public record DriverResponse(
        UUID id, String name, String phone, String licenceNo,
        LocalDate licenceExpiry, DriverStatus status
) {
    public static DriverResponse from(Driver d) {
        return new DriverResponse(d.getId(), d.getName(), d.getPhone(), d.getLicenceNo(),
                d.getLicenceExpiry(), d.getStatus());
    }
}
