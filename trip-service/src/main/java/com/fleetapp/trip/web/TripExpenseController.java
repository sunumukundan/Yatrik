package com.fleetapp.trip.web;

import com.fleetapp.trip.service.TripExpenseService;
import com.fleetapp.trip.web.dto.TripExpenseCreateDto;
import com.fleetapp.trip.web.dto.TripExpenseResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/trips/{tripId}/expenses")
@RequiredArgsConstructor
public class TripExpenseController {

    private final TripExpenseService tripExpenseService;

    @GetMapping
    public List<TripExpenseResponse> list(@PathVariable("tripId") UUID tripId) {
        return tripExpenseService.list(tripId).stream().map(TripExpenseResponse::from).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TripExpenseResponse create(@PathVariable("tripId") UUID tripId,
                                      @Valid @RequestBody TripExpenseCreateDto dto) {
        return TripExpenseResponse.from(tripExpenseService.create(tripId, dto));
    }
}
