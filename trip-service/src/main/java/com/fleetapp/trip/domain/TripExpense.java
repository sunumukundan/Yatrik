package com.fleetapp.trip.domain;

import com.fleetapp.common.domain.TenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "trip_expense", uniqueConstraints = {
        @UniqueConstraint(name = "uq_trip_expense_client_ref", columnNames = {"organisation_id", "client_ref"})
})
@Getter
@Setter
@NoArgsConstructor
public class TripExpense extends TenantEntity {

    @Id
    @JdbcTypeCode(SqlTypes.UUID)
    @GeneratedValue
    private UUID id;

    @Column(name = "trip_id", nullable = false)
    private UUID tripId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExpenseCategory category;

    @Column(nullable = false)
    private long amount; // paise

    @Enumerated(EnumType.STRING)
    @Column(name = "paid_by", nullable = false)
    private PaidByType paidBy;

    @Column(name = "expense_date", nullable = false)
    private LocalDate expenseDate = LocalDate.now();

    @Column(name = "receipt_file_id")
    private UUID receiptFileId;

    @Column(columnDefinition = "TEXT")
    private String note;

    /**
     * Idempotency key the mobile app generates client-side, so a retried offline
     * sync never creates the same expense twice. See uq_trip_expense_client_ref.
     */
    @Column(name = "client_ref", nullable = false)
    private UUID clientRef;
}
