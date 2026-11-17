package com.fleetapp.fleet.service;

import com.fleetapp.common.security.CurrentUser;
import com.fleetapp.fleet.domain.Driver;
import com.fleetapp.fleet.domain.DriverStatus;
import com.fleetapp.fleet.repository.DriverRepository;
import com.fleetapp.fleet.web.dto.DriverCreateDto;
import com.fleetapp.fleet.web.dto.DriverUpdateDto;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DriverService {

    private final DriverRepository driverRepository;

    public List<Driver> list() {
        return driverRepository.findByOrganisationIdAndDeletedAtIsNull(CurrentUser.get().organisationId());
    }

    public Driver get(UUID id) {
        return driverRepository.findByIdAndOrganisationIdAndDeletedAtIsNull(id, CurrentUser.get().organisationId())
                .orElseThrow(() -> new EntityNotFoundException("Driver not found: " + id));
    }

    @Transactional
    public Driver create(DriverCreateDto dto) {
        Driver driver = new Driver();
        driver.setOrganisationId(CurrentUser.get().organisationId());
        driver.setName(dto.name());
        driver.setPhone(dto.phone());
        driver.setLicenceNo(dto.licenceNo());
        driver.setLicenceExpiry(dto.licenceExpiry());
        driver.setCreatedBy(CurrentUser.get().userId());
        return driverRepository.save(driver);
    }

    @Transactional
    public Driver update(UUID id, DriverUpdateDto dto) {
        Driver driver = get(id);
        if (dto.name() != null) {
            driver.setName(dto.name());
        }
        if (dto.phone() != null) {
            driver.setPhone(dto.phone());
        }
        if (dto.licenceNo() != null) {
            driver.setLicenceNo(dto.licenceNo());
        }
        if (dto.licenceExpiry() != null) {
            driver.setLicenceExpiry(dto.licenceExpiry());
        }
        if (dto.status() != null) {
            driver.setStatus(dto.status());
        }
        return driverRepository.save(driver);
    }

    @Transactional
    public void delete(UUID id) {
        Driver driver = get(id);
        driver.setDeletedAt(java.time.Instant.now());
        driverRepository.save(driver);
    }
}
