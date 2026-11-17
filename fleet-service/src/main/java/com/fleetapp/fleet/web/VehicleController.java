package com.fleetapp.fleet.web;

import com.fleetapp.fleet.domain.VehicleStatus;
import com.fleetapp.fleet.service.VehicleService;
import com.fleetapp.fleet.web.dto.VehicleCreateDto;
import com.fleetapp.fleet.web.dto.VehicleResponse;
import com.fleetapp.fleet.web.dto.VehicleUpdateDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/vehicles")
@RequiredArgsConstructor
public class VehicleController {

    private final VehicleService vehicleService;

    @GetMapping
    public List<VehicleResponse> list() {
        return vehicleService.list().stream().map(VehicleResponse::from).toList();
    }

    @GetMapping("/{id}")
    public VehicleResponse get(@PathVariable("id") UUID id) {
        return VehicleResponse.from(vehicleService.get(id));
    }

    /** The vehicles in one state, e.g. the ones currently off the road. */
    @GetMapping("/by-status/{status}")
    public List<VehicleResponse> listByStatus(@PathVariable("status") VehicleStatus status) {
        return vehicleService.listByStatus(status).stream().map(VehicleResponse::from).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public VehicleResponse create(@Valid @RequestBody VehicleCreateDto dto) {
        return VehicleResponse.from(vehicleService.create(dto));
    }

    @PutMapping("/{id}")
    public VehicleResponse update(@PathVariable("id") UUID id,
                                  @Valid @RequestBody VehicleUpdateDto dto) {
        return VehicleResponse.from(vehicleService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("id") UUID id) {
        vehicleService.delete(id);
    }
}
