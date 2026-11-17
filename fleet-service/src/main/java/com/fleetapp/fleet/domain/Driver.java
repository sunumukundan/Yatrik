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

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "driver", uniqueConstraints = {
        @UniqueConstraint(name = "uq_driver_phone_org", columnNames = {"organisation_id", "phone"})
})
@Getter
@Setter
@NoArgsConstructor
public class Driver extends TenantEntity {

    @Id
    @JdbcTypeCode(SqlTypes.UUID)
    @GeneratedValue
    private UUID id;

    /** Nullable: a driver may exist in the fleet before they have an app login. */
    @Column(name = "user_id")
    private UUID userId;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, length = 15)
    private String phone;

    @Column(name = "licence_no", length = 30)
    private String licenceNo;

    @Column(name = "licence_expiry")
    private LocalDate licenceExpiry;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DriverStatus status = DriverStatus.ACTIVE;
}
