package com.fleetapp.trip.web.dto;

import com.fleetapp.trip.domain.ExpenseCategory;
import com.fleetapp.trip.domain.PaidByType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;
import java.util.UUID;

public record TripExpenseCreateDto(
        @NotNull ExpenseCategory category,
        @Positive long amount,
        @NotNull PaidByType paidBy,
        LocalDate expenseDate,
        UUID receiptFileId,
        String note,
        // Required: the mobile app generates this once per expense entry, before the
        // user even has network, and resends the same value on every retry — that's
        // what makes a duplicated offline sync harmless rather than a double expense.
        @NotNull UUID clientRef
) {}
