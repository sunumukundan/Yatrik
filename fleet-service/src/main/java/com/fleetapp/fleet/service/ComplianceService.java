package com.fleetapp.fleet.service;

import com.fleetapp.common.security.CurrentUser;
import com.fleetapp.fleet.repository.DriverDocumentRepository;
import com.fleetapp.fleet.repository.VehicleDocumentRepository;
import com.fleetapp.fleet.web.dto.DriverDocumentResponse;
import com.fleetapp.fleet.web.dto.VehicleDocumentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * The documents that are about to expire, or have already.
 *
 * An expired registration, insurance or fitness certificate is a vehicle that cannot
 * legally run, so this is the report an owner checks before dispatching.
 */
@Service
@RequiredArgsConstructor
public class ComplianceService {

    /** Far enough back to catch anything already expired, since a document may have expired years ago. */
    private static final int LOOKBACK_YEARS = 20;

    private final VehicleDocumentRepository vehicleDocumentRepository;
    private final DriverDocumentRepository driverDocumentRepository;

    /**
     * The documents that fall due inside the window, and have not expired yet.
     */
    public List<VehicleDocumentResponse> vehicleDocumentsExpiringWithin(int days) {
        LocalDate from = LocalDate.now();
        LocalDate to = from.plusDays(days);
        return vehicleDocumentRepository
                .findByOrganisationIdAndExpiryDateBetweenAndDeletedAtIsNull(
                        CurrentUser.get().organisationId(), from, to)
                .stream()
                .map(VehicleDocumentResponse::from)
                .toList();
    }

    public List<DriverDocumentResponse> driverDocumentsExpiringWithin(int days) {
        LocalDate from = LocalDate.now();
        LocalDate to = from.plusDays(days);
        return driverDocumentRepository
                .findByOrganisationIdAndExpiryDateBetweenAndDeletedAtIsNull(
                        CurrentUser.get().organisationId(), from, to)
                .stream()
                .map(DriverDocumentResponse::from)
                .toList();
    }

    /**
     * The documents that have ALREADY expired. Separate from the "expiring soon" list
     * because an expired document is a different problem: the vehicle is not legal to
     * run today, not merely at risk of becoming illegal.
     */
    public List<VehicleDocumentResponse> expiredVehicleDocuments() {
        LocalDate today = LocalDate.now();
        return vehicleDocumentRepository
                .findByOrganisationIdAndExpiryDateBetweenAndDeletedAtIsNull(
                        CurrentUser.get().organisationId(), today.minusYears(LOOKBACK_YEARS), today.minusDays(1))
                .stream()
                .map(VehicleDocumentResponse::from)
                .toList();
    }

    public List<DriverDocumentResponse> expiredDriverDocuments() {
        LocalDate today = LocalDate.now();
        return driverDocumentRepository
                .findByOrganisationIdAndExpiryDateBetweenAndDeletedAtIsNull(
                        CurrentUser.get().organisationId(), today.minusYears(LOOKBACK_YEARS), today.minusDays(1))
                .stream()
                .map(DriverDocumentResponse::from)
                .toList();
    }
}
