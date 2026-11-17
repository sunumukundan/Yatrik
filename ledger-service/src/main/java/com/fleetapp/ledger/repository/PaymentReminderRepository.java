package com.fleetapp.ledger.repository;

import com.fleetapp.ledger.domain.PaymentReminder;
import com.fleetapp.ledger.domain.ReminderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentReminderRepository extends JpaRepository<PaymentReminder, UUID> {

    List<PaymentReminder> findByOrganisationIdAndStatusAndDeletedAtIsNull(
            UUID organisationId, ReminderStatus status);

    List<PaymentReminder> findByOrganisationIdAndDueDateBetweenAndDeletedAtIsNull(
            UUID organisationId, LocalDate from, LocalDate to);

    Optional<PaymentReminder> findByIdAndOrganisationIdAndDeletedAtIsNull(UUID id, UUID organisationId);
}
