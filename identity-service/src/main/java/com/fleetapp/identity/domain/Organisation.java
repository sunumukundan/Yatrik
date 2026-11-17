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

import java.time.Instant;
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

    // White-label / branding fields
    @Column(name = "display_name", length = 200)
    private String displayName;

    @Column(name = "logo_url", length = 500)
    private String logoUrl;

    @Column(name = "primary_color", length = 7)
    private String primaryColor;

    @Column(name = "accent_color", length = 7)
    private String accentColor;

    @Column(name = "support_email", length = 150)
    private String supportEmail;

    @Column(name = "support_phone", length = 20)
    private String supportPhone;

    // License entitlement fields (managed by Super Admin)
    @Column(name = "license_admin_seats")
    private Integer licenseAdminSeats = 1;

    @Column(name = "license_fleet_owner_seats")
    private Integer licenseFleetOwnerSeats = 1;

    @Column(name = "license_start_date")
    private Instant licenseStartDate;

    @Column(name = "license_end_date")
    private Instant licenseEndDate;

    @Column(name = "license_grace_period_days")
    private Integer licenseGracePeriodDays = 7;

    // Feature flags
    @Column(name = "feature_drivers_billable")
    private Boolean featureDriversBillable = false;

    @Column(name = "feature_custom_domain")
    private Boolean featureCustomDomain = false;

    @Column(name = "feature_branded_sender")
    private Boolean featureBrandedSender = false;
}
