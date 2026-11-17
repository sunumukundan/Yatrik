package com.fleetapp.fleet.web;

import com.fleetapp.fleet.service.MaintenanceService;
import com.fleetapp.fleet.web.dto.MaintenanceCreateDto;
import com.fleetapp.fleet.web.dto.MaintenanceResponse;
import com.fleetapp.fleet.web.dto.MaintenanceServiceDto;
import com.fleetapp.fleet.web.dto.WorkOrderCreateDto;
import com.fleetapp.fleet.web.dto.WorkOrderResponse;
import com.fleetapp.fleet.web.dto.WorkOrderUpdateDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Maintenance: the planned schedule and the work orders that carry it out.
 *
 * The two live under separate paths so the intent is obvious in the URL:
 *   /maintenance/...            the schedule
 *   /vehicles/{id}/work-orders  the work being done (and done) on a vehicle
 */
@RestController
@RequiredArgsConstructor
public class MaintenanceController {

    private final MaintenanceService maintenanceService;

    // ------------------------------------------------------------------ schedule

    @GetMapping("/maintenance")
    public List<MaintenanceResponse> listSchedule() {
        return maintenanceService.listAll().stream().map(MaintenanceResponse::from).toList();
    }

    @GetMapping("/maintenance/due-soon")
    public List<MaintenanceResponse> dueSoon(@RequestParam(defaultValue = "30") int days) {
        return maintenanceService.dueWithin(days).stream().map(MaintenanceResponse::from).toList();
    }

    @GetMapping("/vehicles/{vehicleId}/maintenance")
    public List<MaintenanceResponse> listForVehicle(@PathVariable("vehicleId") UUID vehicleId) {
        return maintenanceService.listForVehicle(vehicleId).stream()
                .map(MaintenanceResponse::from).toList();
    }

    @PostMapping("/vehicles/{vehicleId}/maintenance")
    @ResponseStatus(HttpStatus.CREATED)
    public MaintenanceResponse schedule(@PathVariable("vehicleId") UUID vehicleId,
                                        @Valid @RequestBody MaintenanceCreateDto dto) {
        return MaintenanceResponse.from(maintenanceService.schedule(vehicleId, dto));
    }

    @PostMapping("/maintenance/{id}/serviced")
    public MaintenanceResponse markServiced(@PathVariable("id") UUID id,
                                            @Valid @RequestBody MaintenanceServiceDto dto) {
        return MaintenanceResponse.from(maintenanceService.markServiced(id, dto));
    }

    // ------------------------------------------------------------------ work orders

    @GetMapping("/vehicles/{vehicleId}/work-orders")
    public List<WorkOrderResponse> listWorkOrders(@PathVariable("vehicleId") UUID vehicleId) {
        return maintenanceService.listWorkOrders(vehicleId).stream()
                .map(WorkOrderResponse::from).toList();
    }

    @GetMapping("/work-orders/open")
    public List<WorkOrderResponse> listOpenWorkOrders() {
        return maintenanceService.listOpenWorkOrders().stream().map(WorkOrderResponse::from).toList();
    }

    @PostMapping("/vehicles/{vehicleId}/work-orders")
    @ResponseStatus(HttpStatus.CREATED)
    public WorkOrderResponse raise(@PathVariable("vehicleId") UUID vehicleId,
                                   @Valid @RequestBody WorkOrderCreateDto dto) {
        return WorkOrderResponse.from(maintenanceService.raiseWorkOrder(vehicleId, dto));
    }

    @PutMapping("/work-orders/{id}")
    public WorkOrderResponse update(@PathVariable("id") UUID id,
                                    @Valid @RequestBody WorkOrderUpdateDto dto) {
        return WorkOrderResponse.from(maintenanceService.updateWorkOrder(id, dto));
    }
}
