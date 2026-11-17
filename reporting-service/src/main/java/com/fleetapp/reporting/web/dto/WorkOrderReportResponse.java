package com.fleetapp.reporting.web.dto;

import java.time.LocalDate;
import java.util.UUID;

/**
 * An open work order, as the reporting service sees it.
 */
public record WorkOrderReportResponse(
        UUID id,
        UUID vehicleId,
        String description,
        String status,
        LocalDate raisedOn,
        LocalDate scheduledFor,
        Long costAmount,
        String garageName
) {}
