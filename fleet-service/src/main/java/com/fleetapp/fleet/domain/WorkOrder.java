package com.fleetapp.fleet.domain;

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

import java.time.Instant;
import java.time.LocalDate;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * A specific piece of work on a vehicle: a breakdown, a repair, a routine service.
 *
 * A work order is what takes the vehicle off the road. While one is OPEN or
 * IN_PROGRESS the vehicle's status is IN_SERVICE, and the utilisation report counts
 * that time as downtime.
 */
@Entity
@Table(name = "work_order")
@Getter
@Setter
@NoArgsConstructor
public class WorkOrder extends TenantEntity {

    @Id
    @JdbcTypeCode(SqlTypes.UUID)
    @GeneratedValue
    private UUID id;

    @Column(name = "vehicle_id", nullable = false)
    private UUID vehicleId;

    /**
     * The maintenance schedule entry this work order came from, when it came from one.
     * Null for a breakdown raised on the road.
     */
    @Column(name = "maintenance_id")
    private UUID maintenanceId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private MaintenanceType type;

    @Column(nullable = false, length = 150)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WorkOrderStatus status = WorkOrderStatus.OPEN;

    @Column(name = "raised_on", nullable = false)
    private LocalDate raisedOn = LocalDate.now();

    @Column(name = "scheduled_for")
    private LocalDate scheduledFor;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "odometer_km", precision = 10, scale = 2)
    private BigDecimal odometerKm;

    @Column(name = "cost_amount")
    private Long costAmount;

    @Column(name = "garage_name", length = 150)
    private String garageName;

    @Column(columnDefinition = "TEXT")
    private String notes;
}
