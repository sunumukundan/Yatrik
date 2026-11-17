package com.fleetapp.ledger.web.dto;

import com.fleetapp.ledger.domain.EntryType;
import com.fleetapp.ledger.domain.PaymentMode;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;
import java.util.UUID;

public record LedgerEntryCreateDto(
        @NotNull EntryType entryType,
        @Positive long amount,
        LocalDate entryDate,
        UUID tripId,
        PaymentMode mode,
        String reference,
        String note,
        // Optional here, unlike TripExpense — a manual web-dashboard entry has no
        // offline-sync concern. The mobile app should always send one.
        UUID clientRef
) {}
