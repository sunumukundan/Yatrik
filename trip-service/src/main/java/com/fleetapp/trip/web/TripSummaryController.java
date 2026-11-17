package com.fleetapp.trip.web;

import com.fleetapp.trip.service.TripSummaryService;
import com.fleetapp.trip.web.dto.TripSummaryDto;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/**
 * The trip figures a report needs, in one call.
 *
 * The reporting service uses this instead of asking for each trip's profit
 * individually.
 */
@RestController
@RequestMapping("/trips/summary")
@RequiredArgsConstructor
public class TripSummaryController {

    private final TripSummaryService tripSummaryService;

    @GetMapping
    public List<TripSummaryDto> summary(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        LocalDate start = from != null ? from : LocalDate.now().withDayOfMonth(1);
        LocalDate end = to != null ? to : LocalDate.now();
        return tripSummaryService.summarise(start, end);
    }
}
