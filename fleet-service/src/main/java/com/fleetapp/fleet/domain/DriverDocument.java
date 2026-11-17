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
import java.util.UUID;

@Entity
@Table(name = "driver_document")
@Getter
@Setter
@NoArgsConstructor
public class DriverDocument extends TenantEntity {

    @Id
    @JdbcTypeCode(SqlTypes.UUID)
    @GeneratedValue
    private UUID id;

    @Column(name = "driver_id", nullable = false)
    private UUID driverId;

    @Enumerated(EnumType.STRING)
    @Column(name = "doc_type", nullable = false)
    private DocType docType;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(name = "file_id")
    private UUID fileId;

    @Column(name = "reminder_sent_at")
    private Instant reminderSentAt;
}
