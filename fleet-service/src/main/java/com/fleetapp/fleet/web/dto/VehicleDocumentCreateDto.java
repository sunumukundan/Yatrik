package com.fleetapp.fleet.web.dto;

import com.fleetapp.fleet.domain.DocType;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public record VehicleDocumentCreateDto(
        @NotNull DocType docType,
        String docNo,
        LocalDate issueDate,
        LocalDate expiryDate,
        UUID fileId
) {}
