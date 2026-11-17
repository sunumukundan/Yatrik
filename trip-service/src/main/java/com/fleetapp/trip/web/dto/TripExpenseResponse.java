package com.fleetapp.trip.web.dto;

import com.fleetapp.trip.domain.ExpenseCategory;
import com.fleetapp.trip.domain.PaidByType;
import com.fleetapp.trip.domain.TripExpense;

import java.time.LocalDate;
import java.util.UUID;

public record TripExpenseResponse(
        UUID id, UUID tripId, ExpenseCategory category, long amount,
        PaidByType paidBy, LocalDate expenseDate, String note
) {
    public static TripExpenseResponse from(TripExpense e) {
        return new TripExpenseResponse(e.getId(), e.getTripId(), e.getCategory(), e.getAmount(),
                e.getPaidBy(), e.getExpenseDate(), e.getNote());
    }
}
