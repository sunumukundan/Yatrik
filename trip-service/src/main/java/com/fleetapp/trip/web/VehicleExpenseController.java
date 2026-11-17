package com.fleetapp.trip.web;

import com.fleetapp.trip.service.VehicleExpenseService;
import com.fleetapp.trip.web.dto.VehicleExpenseCreateDto;
import com.fleetapp.trip.web.dto.VehicleExpenseResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Vehicle-level costs (EMI, insurance, servicing). Separate from trip expenses,
 * because these are not part of any trip's profit.
 */
@RestController
@RequestMapping("/vehicles/{vehicleId}/expenses")
@RequiredArgsConstructor
public class VehicleExpenseController {

    private final VehicleExpenseService vehicleExpenseService;

    @GetMapping
    public List<VehicleExpenseResponse> list(@PathVariable("vehicleId") UUID vehicleId) {
        return vehicleExpenseService.list(vehicleId).stream().map(VehicleExpenseResponse::from).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public VehicleExpenseResponse create(@PathVariable("vehicleId") UUID vehicleId,
                                         @Valid @RequestBody VehicleExpenseCreateDto dto) {
        return VehicleExpenseResponse.from(vehicleExpenseService.create(vehicleId, dto));
    }
}
