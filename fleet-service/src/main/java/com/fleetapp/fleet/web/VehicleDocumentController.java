package com.fleetapp.fleet.web;

import com.fleetapp.fleet.service.VehicleDocumentService;
import com.fleetapp.fleet.web.dto.VehicleDocumentCreateDto;
import com.fleetapp.fleet.web.dto.VehicleDocumentResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/vehicles/{vehicleId}/documents")
@RequiredArgsConstructor
public class VehicleDocumentController {

    private final VehicleDocumentService vehicleDocumentService;

    @GetMapping
    public List<VehicleDocumentResponse> list(@PathVariable("vehicleId") UUID vehicleId) {
        return vehicleDocumentService.list(vehicleId).stream().map(VehicleDocumentResponse::from).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public VehicleDocumentResponse create(@PathVariable("vehicleId") UUID vehicleId,
                                          @Valid @RequestBody VehicleDocumentCreateDto dto) {
        return VehicleDocumentResponse.from(vehicleDocumentService.create(vehicleId, dto));
    }
}
