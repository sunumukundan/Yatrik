package com.fleetapp.trip.domain;

import com.fleetapp.common.domain.TenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.util.UUID;

/**
 * A cost that belongs to the vehicle rather than to one trip: EMI, insurance premium,
 * scheduled servicing. Never part of a trip's profit — it shows up in the vehicle's
 * running cost instead.
 */
@Entity
@Table(name = "vehicle_expense")
@Getter
@Setter
@NoArgsConstructor
public class VehicleExpense extends TenantEntity {

    @Id
    @JdbcTypeCode(SqlTypes.UUID)
    @GeneratedValue
    private UUID id;

    @Column(name = "vehicle_id", nullable = false)
    private UUID vehicleId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VehicleExpenseCategory category;

    @Column(nullable = false)
    private long amount; // paise

    @Column(name = "expense_date", nullable = false)
    private LocalDate expenseDate = LocalDate.now();

    @Column(columnDefinition = "TEXT")
    private String note;

    @Column(name = "client_ref")
    private UUID clientRef;
}
