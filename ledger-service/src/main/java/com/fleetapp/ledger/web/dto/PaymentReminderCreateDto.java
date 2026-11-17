package com.fleetapp.ledger.web.dto;

import com.fleetapp.ledger.domain.ReminderChannel;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;
import java.util.UUID;

public record PaymentReminderCreateDto(
        @NotNull UUID partyId,
        @Positive long amount,
        LocalDate dueDate,
        ReminderChannel channel
) {}
