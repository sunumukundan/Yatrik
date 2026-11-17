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

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "trip")
@Getter
@Setter
@NoArgsConstructor
public class Trip extends TenantEntity {

    @Id
    @JdbcTypeCode(SqlTypes.UUID)
    @GeneratedValue
    private UUID id;

    @Column(name = "vehicle_id", nullable = false)
    private UUID vehicleId;

    @Column(name = "driver_id", nullable = false)
    private UUID driverId;

    /** The customer, from the ledger service. */
    @Column(name = "party_id")
    private UUID partyId;

    @Column(nullable = false, length = 150)
    private String origin;

    @Column(nullable = false, length = 150)
    private String destination;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "distance_km", precision = 8, scale = 2)
    private BigDecimal distanceKm;

    /** Paise, as BIGINT — never floating point for money. */
    @Column(name = "freight_amount", nullable = false)
    private long freightAmount = 0;

    /**
     * Paid by the CUSTOMER up front toward this trip's freight (paise). Posted to the
     * customer's ledger as a CREDIT when the trip completes. Not a cost, so never in profit.
     */
    @Column(name = "customer_advance", nullable = false)
    private long customerAdvance = 0;

    /**
     * Cash the OWNER gave the DRIVER for this trip's expenses (paise). Stays on the trip;
     * the driver's balance is this minus what the driver spent (TripService.driverCash).
     */
    @Column(name = "driver_advance", nullable = false)
    private long driverAdvance = 0;

    @Column(nullable = false, length = 3)
    private String currency = "INR";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TripStatus status = TripStatus.PLANNED;

    @Column(columnDefinition = "TEXT")
    private String notes;

    /**
     * Set when status -> COMPLETED. Once non-null, TripService refuses further edits
     * to freight/advance/expenses — the DB does not enforce this, the service does.
     */
    @Column(name = "closed_at")
    private Instant closedAt;
}
