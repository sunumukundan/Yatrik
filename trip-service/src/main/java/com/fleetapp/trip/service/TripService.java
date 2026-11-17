package com.fleetapp.trip.service;

import com.fleetapp.common.security.CurrentUser;
import com.fleetapp.trip.client.LedgerClient;
import com.fleetapp.trip.domain.PaidByType;
import com.fleetapp.trip.domain.Trip;
import com.fleetapp.trip.domain.TripExpense;
import com.fleetapp.trip.domain.TripStatus;
import com.fleetapp.trip.exception.InvalidTripException;
import com.fleetapp.trip.exception.TripClosedException;
import com.fleetapp.trip.repository.TripExpenseRepository;
import com.fleetapp.trip.repository.TripRepository;
import com.fleetapp.trip.web.dto.TripCreateDto;
import com.fleetapp.trip.web.dto.TripDriverCashResponse;
import com.fleetapp.trip.web.dto.TripProfitResponse;
import com.fleetapp.trip.web.dto.TripUpdateDto;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TripService {

    private final TripRepository tripRepository;
    private final TripExpenseRepository tripExpenseRepository;
    private final LedgerClient ledgerClient;

    public List<Trip> list() {
        return tripRepository.findByOrganisationIdAndDeletedAtIsNullOrderByCreatedAtDesc(
                CurrentUser.get().organisationId());
    }

    public List<Trip> listByStatus(TripStatus status) {
        return tripRepository.findByOrganisationIdAndStatusAndDeletedAtIsNull(
                CurrentUser.get().organisationId(), status);
    }

    public Trip get(UUID id) {
        return tripRepository.findByIdAndOrganisationIdAndDeletedAtIsNull(id, CurrentUser.get().organisationId())
                .orElseThrow(() -> new EntityNotFoundException("Trip not found: " + id));
    }

    @Transactional
    public Trip create(TripCreateDto dto) {
        // An advance from "the customer" with no customer attached could never reach any
        // ledger, so it would sit on the trip forever. Refuse it up front.
        if (dto.customerAdvance() > 0 && dto.partyId() == null) {
            throw new InvalidTripException(
                    "customerAdvance needs a customer: set partyId, or send customerAdvance as 0");
        }
        Trip trip = new Trip();
        trip.setOrganisationId(CurrentUser.get().organisationId());
        trip.setVehicleId(dto.vehicleId());
        trip.setDriverId(dto.driverId());
        trip.setPartyId(dto.partyId());
        trip.setOrigin(dto.origin());
        trip.setDestination(dto.destination());
        trip.setStartDate(dto.startDate() != null ? dto.startDate() : LocalDate.now());
        trip.setDistanceKm(dto.distanceKm());
        trip.setFreightAmount(dto.freightAmount());
        trip.setCustomerAdvance(dto.customerAdvance());
        trip.setDriverAdvance(dto.driverAdvance());
        trip.setNotes(dto.notes());
        trip.setStatus(TripStatus.PLANNED);
        trip.setCreatedBy(CurrentUser.get().userId());
        return tripRepository.save(trip);
    }

    /**
     * Edits a trip that has not been closed yet.
     *
     * The route, the vehicle and the driver are deliberately not editable: a trip's
     * identity is what the paperwork and the ledger reference. The money fields are
     * editable only while the trip is open, because once it is closed those numbers
     * must not move again.
     */
    @Transactional
    public Trip update(UUID id, TripUpdateDto dto) {
        Trip trip = get(id);
        assertNotClosed(trip);

        if (dto.distanceKm() != null) {
            trip.setDistanceKm(dto.distanceKm());
        }
        if (dto.endDate() != null) {
            trip.setEndDate(dto.endDate());
        }
        if (dto.freightAmount() != null) {
            trip.setFreightAmount(dto.freightAmount());
        }
        if (dto.customerAdvance() != null) {
            trip.setCustomerAdvance(dto.customerAdvance());
        }
        if (dto.driverAdvance() != null) {
            trip.setDriverAdvance(dto.driverAdvance());
        }
        if (dto.notes() != null) {
            trip.setNotes(dto.notes());
        }
        if (dto.status() != null) {
            trip.setStatus(dto.status());
        }
        return tripRepository.save(trip);
    }

    /**
     * Marks a trip COMPLETED and sets closedAt. Once closed, every write path in
     * this service (and TripExpenseService) must refuse further changes — the
     * database has no trigger enforcing this, so every mutating method here checks
     * assertNotClosed() first. This gives owners an audit trail they can trust:
     * a completed trip's numbers never move again except via an explicit adjustment
     * (not yet built) rather than a silent edit.
     *
     * If the trip has a customer (partyId), its freight is first recorded on that
     * customer's ledger. That call happens BEFORE closing the trip, on purpose:
     *  - ledger down  -> LedgerSyncException, trip stays open, the owner can just retry.
     *  - the other order would leave a locked trip with no receivable and no way to fix it.
     *  - crash between the ledger call and the save -> retry is safe, because the ledger
     *    entry's clientRef is derived from the trip id (see LedgerClient).
     * Deliberately not @Transactional: no database transaction should stay open across
     * an HTTP call.
     *
     * Two entries are posted for a trip that has a customer: the freight as a DEBIT (what
     * they owe) and customerAdvance, if any, as a CREDIT (what they already paid). Both
     * land at completion, so the ledger shows completed trips only, and the balance nets
     * out to freight minus advance. Both are idempotent (LedgerRefs), and the trip closes
     * only after both succeed, so a failure between them is fixed by simply retrying.
     * driverAdvance is NOT posted: it is cash between owner and driver, tracked on the
     * trip itself (see driverCash).
     */
    public Trip complete(UUID id) {
        Trip trip = get(id);
        assertNotClosed(trip);

        if (trip.getPartyId() != null) {
            String route = trip.getOrigin() + " to " + trip.getDestination();
            if (trip.getFreightAmount() > 0) {
                ledgerClient.postFreightDebit(
                        trip.getPartyId(), trip.getId(), trip.getFreightAmount(), "Freight " + route);
            }
            if (trip.getCustomerAdvance() > 0) {
                ledgerClient.postCustomerAdvanceCredit(
                        trip.getPartyId(), trip.getId(), trip.getCustomerAdvance(), "Advance received " + route);
            }
        }

        trip.setStatus(TripStatus.COMPLETED);
        trip.setClosedAt(Instant.now());
        if (trip.getEndDate() == null) {
            trip.setEndDate(LocalDate.now());
        }
        return tripRepository.save(trip);
    }

    public TripProfitResponse profitFor(UUID id) {
        Trip trip = get(id);
        long totalExpenses = tripExpenseRepository.sumAmountByTripId(id);
        long profit = trip.getFreightAmount() - totalExpenses;
        return new TripProfitResponse(trip.getId(), trip.getFreightAmount(), totalExpenses, profit);
    }

    /**
     * The driver's cash position for this trip. Only expenses the DRIVER paid count against
     * the advance: what the owner paid directly never touched the driver's pocket. Like
     * profit, this reads live, so it keeps working after the trip is closed.
     */
    public TripDriverCashResponse driverCash(UUID id) {
        Trip trip = get(id);
        long spentByDriver = tripExpenseRepository
                .findByTripIdAndDeletedAtIsNullOrderByExpenseDateDesc(id).stream()
                .filter(e -> e.getPaidBy() == PaidByType.DRIVER)
                .mapToLong(TripExpense::getAmount)
                .sum();
        return new TripDriverCashResponse(
                trip.getId(), trip.getDriverAdvance(), spentByDriver, trip.getDriverAdvance() - spentByDriver);
    }

    public void assertNotClosed(Trip trip) {
        if (trip.getClosedAt() != null) {
            throw new TripClosedException("Trip is closed and can no longer be modified: " + trip.getId());
        }
    }
}
