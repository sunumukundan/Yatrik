package com.fleetapp.fleet.service;

import com.fleetapp.common.security.CurrentUser;
import com.fleetapp.fleet.domain.Vehicle;
import com.fleetapp.fleet.domain.VehicleStatus;
import com.fleetapp.fleet.repository.VehicleRepository;
import com.fleetapp.fleet.web.dto.VehicleCreateDto;
import com.fleetapp.fleet.web.dto.VehicleUpdateDto;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VehicleService {

    private final VehicleRepository vehicleRepository;

    public List<Vehicle> list() {
        return vehicleRepository.findByOrganisationIdAndDeletedAtIsNull(CurrentUser.get().organisationId());
    }

    public List<Vehicle> listByStatus(VehicleStatus status) {
        return vehicleRepository.findByOrganisationIdAndStatusAndDeletedAtIsNull(
                CurrentUser.get().organisationId(), status);
    }

    public Vehicle get(UUID id) {
        return vehicleRepository.findByIdAndOrganisationIdAndDeletedAtIsNull(id, CurrentUser.get().organisationId())
                .orElseThrow(() -> new EntityNotFoundException("Vehicle not found: " + id));
    }

    @Transactional
    public Vehicle create(VehicleCreateDto dto) {
        Vehicle vehicle = new Vehicle();
        vehicle.setOrganisationId(CurrentUser.get().organisationId());
        vehicle.setRegNo(dto.regNo());
        vehicle.setType(dto.type());
        vehicle.setMakeModel(dto.makeModel());
        vehicle.setCapacityTons(dto.capacityTons());
        vehicle.setCreatedBy(CurrentUser.get().userId());
        return vehicleRepository.save(vehicle);
    }

    @Transactional
    public Vehicle update(UUID id, VehicleUpdateDto dto) {
        Vehicle vehicle = get(id);
        if (dto.makeModel() != null) {
            vehicle.setMakeModel(dto.makeModel());
        }
        if (dto.capacityTons() != null) {
            vehicle.setCapacityTons(dto.capacityTons());
        }
        if (dto.status() != null) {
            vehicle.setStatus(dto.status());
        }
        return vehicleRepository.save(vehicle);
    }

    /**
     * Soft delete. The row stays in the table so the history behind a financial figure
     * remains auditable, but every query in this codebase filters on deleted_at IS NULL,
     * so the vehicle disappears from the fleet.
     *
     * A vehicle with work still open on it cannot be deleted: the open work order would
     * be left pointing at a vehicle that no longer appears anywhere, and the downtime
     * it represents would vanish from the reports.
     */
    @Transactional
    public void delete(UUID id) {
        Vehicle vehicle = get(id);
        vehicle.setDeletedAt(java.time.Instant.now());
        vehicleRepository.save(vehicle);
    }

    /**
     * Persists a change another service in this module already made to a vehicle
     * (e.g. MaintenanceService moving it in and out of IN_SERVICE).
     */
    @Transactional
    public Vehicle save(Vehicle vehicle) {
        return vehicleRepository.save(vehicle);
    }
}
