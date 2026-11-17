package com.fleetapp.identity.repository;

import com.fleetapp.identity.domain.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    // Deliberately returns revoked/replaced rows too: RefreshTokenPolicy decides what
    // each state means, so the rule lives in one place instead of in query names.
    Optional<RefreshToken> findByTokenHash(String tokenHash);
}
