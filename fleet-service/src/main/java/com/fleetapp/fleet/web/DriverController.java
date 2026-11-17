package com.fleetapp.fleet.web;

import com.fleetapp.fleet.service.DriverService;
import com.fleetapp.fleet.web.dto.DriverCreateDto;
import com.fleetapp.fleet.web.dto.DriverResponse;
import com.fleetapp.fleet.web.dto.DriverUpdateDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/drivers")
@RequiredArgsConstructor
public class DriverController {

    private final DriverService driverService;

    @GetMapping
    public List<DriverResponse> list() {
        return driverService.list().stream().map(DriverResponse::from).toList();
    }

    @GetMapping("/{id}")
    public DriverResponse get(@PathVariable("id") UUID id) {
        return DriverResponse.from(driverService.get(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DriverResponse create(@Valid @RequestBody DriverCreateDto dto) {
        return DriverResponse.from(driverService.create(dto));
    }

    @PutMapping("/{id}")
    public DriverResponse update(@PathVariable("id") UUID id,
                                  @Valid @RequestBody DriverUpdateDto dto) {
        return DriverResponse.from(driverService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("id") UUID id) {
        driverService.delete(id);
    }
}
