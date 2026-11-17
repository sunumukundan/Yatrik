package com.fleetapp.trip.repository;

import com.fleetapp.trip.domain.PaidByType;
import com.fleetapp.trip.domain.TripExpense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface TripExpenseRepository extends JpaRepository<TripExpense, UUID> {

    List<TripExpense> findByTripIdAndDeletedAtIsNullOrderByExpenseDateDesc(UUID tripId);

    /**
     * Used by TripService.profitFor() to sum expenses without loading full entities.
     */
    @Query("select coalesce(sum(e.amount), 0) from TripExpense e "
            + "where e.tripId = :tripId and e.deletedAt is null")
    long sumAmountByTripId(UUID tripId);

    /**
     * The same total, split by who paid. The owner's own spending is a cost of the
     * trip; the driver's spending comes out of the driver's advance.
     */
    @Query("select coalesce(sum(e.amount), 0) from TripExpense e "
            + "where e.tripId = :tripId and e.paidBy = :paidBy and e.deletedAt is null")
    long sumAmountByTripIdAndPaidBy(UUID tripId, PaidByType paidBy);

    /**
     * All expenses of a trip, for the reporting service to aggregate over a period.
     */
    List<TripExpense> findByOrganisationIdAndExpenseDateBetweenAndDeletedAtIsNull(
            UUID organisationId, LocalDate from, LocalDate to);
}
