package com.fleetapp.ledger.web.dto;

import com.fleetapp.ledger.domain.EntryType;
import com.fleetapp.ledger.domain.LedgerEntry;
import com.fleetapp.ledger.domain.PaymentMode;

import java.time.LocalDate;
import java.util.UUID;

public record LedgerEntryResponse(
        UUID id, UUID partyId, UUID tripId, EntryType entryType, long amount,
        LocalDate entryDate, PaymentMode mode, String reference, String note
) {
    public static LedgerEntryResponse from(LedgerEntry e) {
        return new LedgerEntryResponse(e.getId(), e.getPartyId(), e.getTripId(), e.getEntryType(),
                e.getAmount(), e.getEntryDate(), e.getMode(), e.getReference(), e.getNote());
    }
}
