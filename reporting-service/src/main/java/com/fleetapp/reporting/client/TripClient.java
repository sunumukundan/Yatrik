package com.fleetapp.reporting.client;

import com.fleetapp.reporting.web.dto.TripSummaryResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Reads the trip service's period summary.
 *
 * One call for the whole period, not one per trip: a busy fleet has a lot of trips,
 * and a report that made a call for each of them would be unusably slow.
 */
@Component
public class TripClient extends ServiceClient {

    public TripClient(@Value("${app.trip.base-url}") String baseUrl) {
        super(baseUrl);
    }

    public List<TripSummaryResponse> summary(LocalDate from, LocalDate to) {
        TripSummaryResponse[] trips = get("/trips/summary",
                Map.of("from", from.toString(), "to", to.toString()),
                TripSummaryResponse[].class);
        return trips == null ? List.of() : List.of(trips);
    }
}
