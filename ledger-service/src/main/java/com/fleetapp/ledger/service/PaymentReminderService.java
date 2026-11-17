package com.fleetapp.ledger.service;

import com.fleetapp.common.security.CurrentUser;
import com.fleetapp.ledger.domain.PaymentReminder;
import com.fleetapp.ledger.domain.ReminderStatus;
import com.fleetapp.ledger.repository.PaymentReminderRepository;
import com.fleetapp.ledger.web.dto.PaymentReminderCreateDto;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Payment reminders: what the owner owes a customer, or is owed by one, and whether
 * we have chased it yet.
 */
@Service
@RequiredArgsConstructor
public class PaymentReminderService {

    private final PaymentReminderRepository paymentReminderRepository;
    private final PartyService partyService; // ownership check

    public List<PaymentReminder> list() {
        return paymentReminderRepository.findByOrganisationIdAndStatusAndDeletedAtIsNull(
                CurrentUser.get().organisationId(), ReminderStatus.PENDING);
    }

    /**
     * The reminders that fall due inside the window — the "chase these today" list.
     */
    public List<PaymentReminder> dueWithin(int days) {
        LocalDate from = LocalDate.now();
        LocalDate to = from.plusDays(days);
        return paymentReminderRepository.findByOrganisationIdAndDueDateBetweenAndDeletedAtIsNull(
                CurrentUser.get().organisationId(), from, to);
    }

    @Transactional
    public PaymentReminder create(PaymentReminderCreateDto dto) {
        partyService.findEntity(dto.partyId()); // 404 if not in this org

        PaymentReminder reminder = new PaymentReminder();
        reminder.setOrganisationId(CurrentUser.get().organisationId());
        reminder.setPartyId(dto.partyId());
        reminder.setDueDate(dto.dueDate() != null ? dto.dueDate() : LocalDate.now());
        reminder.setAmount(dto.amount());
        reminder.setChannel(dto.channel());
        reminder.setStatus(ReminderStatus.PENDING);
        reminder.setCreatedBy(CurrentUser.get().userId());
        return paymentReminderRepository.save(reminder);
    }

    /**
     * Marks a reminder as sent. The delivery channel is not wired up yet, so this
     * records the attempt rather than confirming delivery.
     */
    @Transactional
    public PaymentReminder markSent(UUID id, boolean success) {
        PaymentReminder reminder = paymentReminderRepository
                .findByIdAndOrganisationIdAndDeletedAtIsNull(id, CurrentUser.get().organisationId())
                .orElseThrow(() -> new EntityNotFoundException("Payment reminder not found: " + id));

        reminder.setStatus(success ? ReminderStatus.SENT : ReminderStatus.FAILED);
        if (success) {
            reminder.setSentAt(Instant.now());
        }
        return paymentReminderRepository.save(reminder);
    }
}
