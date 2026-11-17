package com.fleetapp.trip.web;

import com.fleetapp.trip.domain.TripStatus;
import com.fleetapp.trip.service.TripService;
import com.fleetapp.trip.web.dto.TripCreateDto;
import com.fleetapp.trip.web.dto.TripDriverCashResponse;
import com.fleetapp.trip.web.dto.TripProfitResponse;
import com.fleetapp.trip.web.dto.TripResponse;
import com.fleetapp.trip.web.dto.TripUpdateDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/trips")
@RequiredArgsConstructor
public class TripController {

    private final TripService tripService;

    @GetMapping
    public List<TripResponse> list() {
        return tripService.list().stream().map(TripResponse::from).toList();
    }

    @GetMapping("/{id}")
    public TripResponse get(@PathVariable("id") UUID id) {
        return TripResponse.from(tripService.get(id));
    }

    @GetMapping("/by-status/{status}")
    public List<TripResponse> listByStatus(@PathVariable("status") TripStatus status) {
        return tripService.listByStatus(status).stream().map(TripResponse::from).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TripResponse create(@Valid @RequestBody TripCreateDto dto) {
        return TripResponse.from(tripService.create(dto));
    }

    @PutMapping("/{id}")
    public TripResponse update(@PathVariable("id") UUID id,
                               @Valid @RequestBody TripUpdateDto dto) {
        return TripResponse.from(tripService.update(id, dto));
    }

    @PostMapping("/{id}/complete")
    public TripResponse complete(@PathVariable("id") UUID id) {
        return TripResponse.from(tripService.complete(id));
    }

    @GetMapping("/{id}/driver-cash")
    public TripDriverCashResponse driverCash(@PathVariable("id") UUID id) {
        return tripService.driverCash(id);
    }

    @GetMapping("/{id}/profit")
    public TripProfitResponse profit(@PathVariable("id") UUID id) {
        return tripService.profitFor(id);
    }
}
