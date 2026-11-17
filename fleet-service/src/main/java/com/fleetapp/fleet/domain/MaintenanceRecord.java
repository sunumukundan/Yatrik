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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * A scheduled service for a vehicle: what kind of work, when it falls due and whether
 * it has been done.
 *
 * The schedule is the plan; the work order is the execution. Keeping them apart means
 * a service can be scheduled months ahead without a garage booking, and a breakdown
 * can be worked on without a schedule entry ever existing.
 */
@Entity
@Table(name = "maintenance_record")
@Getter
@Setter
@NoArgsConstructor
public class MaintenanceRecord extends TenantEntity {

    @Id
    @JdbcTypeCode(SqlTypes.UUID)
    @GeneratedValue
    private UUID id;

    @Column(name = "vehicle_id", nullable = false)
    private UUID vehicleId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private MaintenanceType type;

    @Column(nullable = false, length = 150)
    private String description;

    /**
     * When the service falls due by date. Set when the schedule is calendar-driven.
     */
    @Column(name = "due_date")
    private LocalDate dueDate;

    /**
     * When the service falls due by distance. Set when the schedule is mileage-driven.
     * A schedule may use either, or both — whichever hits first is what the owner sees
     * as "due".
     */
    @Column(name = "due_odometer_km", precision = 10, scale = 2)
    private BigDecimal dueOdometerKm;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ServiceScheduleStatus status = ServiceScheduleStatus.PENDING;

    /**
     * The date the service was actually carried out. Null while the work is pending.
     */
    @Column(name = "serviced_on")
    private LocalDate servicedOn;

    /**
     * The odometer reading when the service was carried out. This becomes the
     * vehicle's last_service_odometer_km, which is the baseline the next mileage-based
     * service is measured from.
     */
    @Column(name = "serviced_at_odometer_km", precision = 10, scale = 2)
    private BigDecimal servicedAtOdometerKm;

    @Column(name = "cost_amount")
    private Long costAmount;

    @Column(name = "garage_name", length = 150)
    private String garageName;

    @Column(columnDefinition = "TEXT")
    private String notes;
}
