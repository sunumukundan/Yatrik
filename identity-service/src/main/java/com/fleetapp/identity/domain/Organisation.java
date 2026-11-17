package com.fleetapp.identity.domain;

import com.fleetapp.common.domain.AuditedEntity;
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

/**
 * A tenant. Every other service scopes its data by organisation_id, and the
 * organisation is created here on the owner's first login.
 *
 * The organisation itself is not tenant-scoped — it IS the tenant — so it does not
 * extend TenantEntity; it only carries the audit columns.
 */
@Entity
@Table(name = "organisation")
@Getter
@Setter
@NoArgsConstructor
public class Organisation extends AuditedEntity {

    @Id
    @JdbcTypeCode(SqlTypes.UUID)
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(name = "gst_no", length = 20)
    private String gstNo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrgStatus status = OrgStatus.TRIAL;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubscriptionPlan plan = SubscriptionPlan.FREE;

    @Column(name = "default_language", nullable = false, length = 5)
    private String defaultLanguage = "en";
}
