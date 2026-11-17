package com.fleetapp.fleet.service;

import com.fleetapp.common.security.CurrentUser;
import com.fleetapp.fleet.domain.VehicleDocument;
import com.fleetapp.fleet.repository.VehicleDocumentRepository;
import com.fleetapp.fleet.web.dto.VehicleDocumentCreateDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VehicleDocumentService {

    private final VehicleDocumentRepository vehicleDocumentRepository;
    private final VehicleService vehicleService; // also confirms the vehicle belongs to this org

    public List<VehicleDocument> list(UUID vehicleId) {
        vehicleService.get(vehicleId); // throws 404 if not in this org
        return vehicleDocumentRepository.findByVehicleIdAndDeletedAtIsNull(vehicleId);
    }

    @Transactional
    public VehicleDocument create(UUID vehicleId, VehicleDocumentCreateDto dto) {
        vehicleService.get(vehicleId); // ownership check

        VehicleDocument doc = new VehicleDocument();
        doc.setOrganisationId(CurrentUser.get().organisationId());
        doc.setVehicleId(vehicleId);
        doc.setDocType(dto.docType());
        doc.setDocNo(dto.docNo());
        doc.setIssueDate(dto.issueDate());
        doc.setExpiryDate(dto.expiryDate());
        doc.setFileId(dto.fileId());
        return vehicleDocumentRepository.save(doc);
    }
}
