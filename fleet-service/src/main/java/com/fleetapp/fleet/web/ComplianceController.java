package com.fleetapp.fleet.web;

import com.fleetapp.fleet.service.ComplianceService;
import com.fleetapp.fleet.web.dto.DriverDocumentResponse;
import com.fleetapp.fleet.web.dto.VehicleDocumentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Document compliance: what is expiring, and what has already expired.
 */
@RestController
@RequestMapping("/compliance")
@RequiredArgsConstructor
public class ComplianceController {

    private final ComplianceService complianceService;

    @GetMapping("/vehicle-documents/expiring")
    public List<VehicleDocumentResponse> vehicleDocumentsExpiring(
            @RequestParam(defaultValue = "30") int days) {
        return complianceService.vehicleDocumentsExpiringWithin(days);
    }

    @GetMapping("/driver-documents/expiring")
    public List<DriverDocumentResponse> driverDocumentsExpiring(
            @RequestParam(defaultValue = "30") int days) {
        return complianceService.driverDocumentsExpiringWithin(days);
    }

    @GetMapping("/vehicle-documents/expired")
    public List<VehicleDocumentResponse> expiredVehicleDocuments() {
        return complianceService.expiredVehicleDocuments();
    }

    @GetMapping("/driver-documents/expired")
    public List<DriverDocumentResponse> expiredDriverDocuments() {
        return complianceService.expiredDriverDocuments();
    }
}
