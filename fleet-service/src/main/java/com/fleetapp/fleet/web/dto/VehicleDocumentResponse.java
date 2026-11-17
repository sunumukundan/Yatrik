package com.fleetapp.fleet.web.dto;

import com.fleetapp.fleet.domain.DocType;
import com.fleetapp.fleet.domain.VehicleDocument;

import java.time.LocalDate;
import java.util.UUID;

public record VehicleDocumentResponse(
        UUID id, UUID vehicleId, DocType docType, String docNo,
        LocalDate issueDate, LocalDate expiryDate, UUID fileId
) {
    public static VehicleDocumentResponse from(VehicleDocument d) {
        return new VehicleDocumentResponse(d.getId(), d.getVehicleId(), d.getDocType(), d.getDocNo(),
                d.getIssueDate(), d.getExpiryDate(), d.getFileId());
    }
}
