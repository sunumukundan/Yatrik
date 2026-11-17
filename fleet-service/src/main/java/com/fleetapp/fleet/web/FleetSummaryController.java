package com.fleetapp.fleet.web;

import com.fleetapp.fleet.service.FleetSummaryService;
import com.fleetapp.fleet.web.dto.FleetSummaryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/fleet")
@RequiredArgsConstructor
public class FleetSummaryController {

    private final FleetSummaryService fleetSummaryService;

    /**
     * The dashboard headline: vehicle and driver counts, what is off the road, and
     * how much work is outstanding.
     */
    @GetMapping("/summary")
    public FleetSummaryResponse summary(@RequestParam(defaultValue = "30") int dueSoonDays) {
        return fleetSummaryService.summary(dueSoonDays);
    }
}
