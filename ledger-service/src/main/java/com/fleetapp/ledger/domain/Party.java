package com.fleetapp.ledger.domain;

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

import java.util.UUID;

@Entity
@Table(name = "party")
@Getter
@Setter
@NoArgsConstructor
public class Party extends TenantEntity {

    @Id
    @JdbcTypeCode(SqlTypes.UUID)
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 15)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PartyType type;

    @Column(name = "gst_no", length = 20)
    private String gstNo;

    /** Paise. Positive = they owe us (seeded balance before this ledger existed). */
    @Column(name = "opening_balance", nullable = false)
    private long openingBalance = 0;
}
