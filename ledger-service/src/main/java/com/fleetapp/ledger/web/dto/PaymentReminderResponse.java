package com.fleetapp.ledger.web.dto;

import com.fleetapp.ledger.domain.PaymentReminder;
import com.fleetapp.ledger.domain.ReminderChannel;
import com.fleetapp.ledger.domain.ReminderStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record PaymentReminderResponse(
        UUID id, UUID partyId, LocalDate dueDate, long amount,
        ReminderChannel channel, ReminderStatus status, Instant sentAt
) {
    public static PaymentReminderResponse from(PaymentReminder r) {
        return new PaymentReminderResponse(r.getId(), r.getPartyId(), r.getDueDate(), r.getAmount(),
                r.getChannel(), r.getStatus(), r.getSentAt());
    }
}
