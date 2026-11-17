package com.fleetapp.trip.service;

import com.fleetapp.common.security.CurrentUser;
import com.fleetapp.trip.domain.PaidByType;
import com.fleetapp.trip.domain.Trip;
import com.fleetapp.trip.domain.TripExpense;
import com.fleetapp.trip.repository.TripExpenseRepository;
import com.fleetapp.trip.repository.TripRepository;
import com.fleetapp.trip.web.dto.TripSummaryDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * The trip figures a report needs, computed in one pass.
 *
 * The reporting service calls this once for a period instead of asking for each
 * trip's profit separately — a per-trip call would be one HTTP round trip per trip,
 * and a busy fleet has a lot of trips.
 */
@Service
@RequiredArgsConstructor
public class TripSummaryService {

    private final TripRepository tripRepository;
    private final TripExpenseRepository tripExpenseRepository;

    @Transactional(readOnly = true)
    public List<TripSummaryDto> summarise(LocalDate from, LocalDate to) {
        UUID orgId = CurrentUser.get().organisationId();
        List<Trip> trips = tripRepository.findByOrganisationIdAndStartDateBetweenAndDeletedAtIsNull(
                orgId, from, to);
        if (trips.isEmpty()) {
            return List.of();
        }

        // One query for every trip's expenses, grouped in memory. Doing this per trip
        // would mean an N+1 query pattern on the hottest reporting path.
        Map<UUID, List<TripExpense>> expensesByTrip = tripExpenseRepository
                .findByOrganisationIdAndExpenseDateBetweenAndDeletedAtIsNull(orgId, from, to)
                .stream()
                .collect(Collectors.groupingBy(TripExpense::getTripId));

        return trips.stream()
                .map(trip -> toSummary(trip, expensesByTrip.getOrDefault(trip.getId(), List.of())))
                .toList();
    }

    private TripSummaryDto toSummary(Trip trip, List<TripExpense> expenses) {
        long total = expenses.stream().mapToLong(TripExpense::getAmount).sum();
        long byDriver = expenses.stream()
                .filter(e -> e.getPaidBy() == PaidByType.DRIVER)
                .mapToLong(TripExpense::getAmount)
                .sum();

        return new TripSummaryDto(
                trip.getId(),
                trip.getVehicleId(),
                trip.getDriverId(),
                trip.getPartyId(),
                trip.getStartDate(),
                trip.getEndDate(),
                trip.getDistanceKm(),
                trip.getFreightAmount(),
                trip.getCustomerAdvance(),
                trip.getDriverAdvance(),
                total,
                trip.getFreightAmount() - total,
                byDriver);
    }
}
