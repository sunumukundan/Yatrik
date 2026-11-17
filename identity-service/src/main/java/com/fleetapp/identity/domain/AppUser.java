package com.fleetapp.identity.domain;

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

import java.util.UUID;

@Entity
@Table(name = "app_user", uniqueConstraints = {
        @UniqueConstraint(name = "uq_user_phone_org", columnNames = {"organisation_id", "phone"}),
        @UniqueConstraint(name = "uq_user_email", columnNames = {"email"})
})
@Getter
@Setter
@NoArgsConstructor
public class AppUser extends TenantEntity {

    @Id
    @JdbcTypeCode(SqlTypes.UUID)
    @GeneratedValue
    private UUID id;

    @Column(name = "email", nullable = false, length = 150)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "phone", nullable = false, length = 15)
    private String phone;

    @Column(nullable = false, length = 150)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    @Column(nullable = false, length = 5)
    private String language = "en";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status = UserStatus.ACTIVE;

    @Column(name = "created_by")
    private UUID createdBy;

    public AppUser(String email, String passwordHash, String phone, String name, UserRole role) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.phone = phone;
        this.name = name;
        this.role = role;
        this.language = "en";
        this.status = UserStatus.ACTIVE;
    }

    public void setCreatedBy(AppUser createdBy) {
        this.createdBy = createdBy.getId();
    }

    public void setCreatedBy(UUID createdById) {
        this.createdBy = createdById;
    }
}
