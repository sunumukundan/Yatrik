package com.fleetapp.fleet.domain;

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

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "vehicle", uniqueConstraints = {
        @UniqueConstraint(name = "uq_vehicle_reg_org", columnNames = {"organisation_id", "reg_no"})
})
@Getter
@Setter
@NoArgsConstructor
public class Vehicle extends TenantEntity {

    @Id
    @JdbcTypeCode(SqlTypes.UUID)
    @GeneratedValue
    private UUID id;

    @Column(name = "reg_no", nullable = false, length = 20)
    private String regNo;

    @Column(nullable = false, length = 50)
    private String type;

    @Column(name = "make_model", length = 100)
    private String makeModel;

    @Column(name = "capacity_tons", precision = 6, scale = 2)
    private BigDecimal capacityTons;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VehicleStatus status = VehicleStatus.IDLE;

    /**
     * The driver currently assigned to this vehicle. A plain UUID rather than a JPA
     * association on purpose: the driver row lives in the same schema, but keeping it
     * as a bare id means the vehicle can be read without loading the driver, and the
     * assignment can be set even for a driver created moments ago in another request.
     */
    @Column(name = "current_driver_id")
    private UUID currentDriverId;

    /**
     * Odometer reading at the last recorded service. The maintenance schedule is
     * measured against this, so a service bumps it and the next due date moves.
     */
    @Column(name = "last_service_odometer_km")
    private BigDecimal lastServiceOdometerKm;
}
