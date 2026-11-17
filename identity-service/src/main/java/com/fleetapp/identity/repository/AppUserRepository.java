package com.fleetapp.identity.repository;

import com.fleetapp.identity.domain.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AppUserRepository extends JpaRepository<AppUser, UUID> {

    Optional<AppUser> findByOrganisationIdAndPhoneAndDeletedAtIsNull(UUID organisationId, String phone);

    Optional<AppUser> findByEmailAndDeletedAtIsNull(String email);

    Optional<AppUser> findFirstByEmailAndDeletedAtIsNull(String email);

    Optional<AppUser> findFirstByPhoneAndDeletedAtIsNull(String phone);

    boolean existsByEmailAndDeletedAtIsNull(String email);

    Optional<AppUser> findByIdAndDeletedAtIsNull(UUID id);

    List<AppUser> findAllByCreatedBy(UUID createdBy);

    long countByCreatedBy(UUID createdBy);
}
