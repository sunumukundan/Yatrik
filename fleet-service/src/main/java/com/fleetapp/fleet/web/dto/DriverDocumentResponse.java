package com.fleetapp.fleet.web.dto;

import com.fleetapp.fleet.domain.DocType;
import com.fleetapp.fleet.domain.DriverDocument;

import java.time.LocalDate;
import java.util.UUID;

public record DriverDocumentResponse(
        UUID id, UUID driverId, DocType docType, LocalDate expiryDate, UUID fileId
) {
    public static DriverDocumentResponse from(DriverDocument d) {
        return new DriverDocumentResponse(d.getId(), d.getDriverId(), d.getDocType(),
                d.getExpiryDate(), d.getFileId());
    }
}
