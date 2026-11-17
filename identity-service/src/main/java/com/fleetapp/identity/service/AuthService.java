package com.fleetapp.identity.service;

import com.fleetapp.identity.domain.*;
import com.fleetapp.identity.exception.InvalidRefreshTokenException;
import com.fleetapp.identity.repository.AppUserRepository;
import com.fleetapp.identity.repository.OrganisationRepository;
import com.fleetapp.identity.repository.RefreshTokenRepository;
import com.fleetapp.identity.security.JwtService;
import com.fleetapp.identity.security.RefreshTokenPolicy;
import com.fleetapp.identity.security.RefreshTokenPolicy.Verdict;
import com.fleetapp.identity.security.TokenHasher;
import com.fleetapp.identity.web.dto.AuthResponse;
import com.fleetapp.identity.web.dto.OtpVerifyDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private static final int REFRESH_TOKEN_DAYS = 30;

    private final OtpService otpService;
    private final AppUserRepository appUserRepository;
    private final OrganisationRepository organisationRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;
    private final TokenHasher tokenHasher;
    private final PasswordEncoder passwordEncoder;
    private final SecureRandom random = new SecureRandom();

    // Password encoder utilities (for backward compatibility)
    public String encodePassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    public boolean matchesPassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    // Role claim helper for audit functionality - moved to JwtService
    public void addRoleClaim(String role) {
        // Role claim addition is handled in JwtService.generateAccessToken
        // This method can be used for audit trail operations
    }


    public void requestOtp(String identifier) {
        otpService.requestOtp(identifier);
    }

    /**
     * Verifies the OTP, then either logs in an existing user or — if this identifier has
     * never been seen — creates a new organisation and an OWNER user for it.
     * MVP assumption: first-time verification always creates an OWNER + a new org.
     * Inviting MANAGER/DRIVER/ACCOUNTANT users is a separate, authenticated flow
     * (not built yet) that creates the app_user row ahead of their first OTP login.
     */
    @Transactional
    public AuthResponse verifyAndLogin(OtpVerifyDto dto) {
        String identifier = dto.getPreferredIdentifier();

        if (!otpService.verifyOtp(identifier, dto.otp())) {
            throw new IllegalArgumentException("Invalid or expired OTP");
        }

        // Look up user by the same identifier (email or phone)
        AppUser user = appUserRepository.findFirstByEmailAndDeletedAtIsNull(identifier)
                .or(() -> appUserRepository.findFirstByPhoneAndDeletedAtIsNull(identifier))
                .orElseGet(() -> createOwnerAndOrganisation(dto, identifier));

        return buildAuthResponse(user);
    }

    // NEW EMAIL/PASSWORD AUTHENTICATION METHODS
    @Transactional
    public AuthResponse login(String email, String password) {
        AppUser user = appUserRepository.findByEmailAndDeletedAtIsNull(email)
            .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new IllegalArgumentException("Account is not active");
        }

        String accessToken = jwtService.generateAccessToken(
            user.getId(),
            user.getOrganisationId(),
            user.getRole().name()
        );
        String refreshToken = issueRefreshToken(user.getId());

        return new AuthResponse(
            accessToken, refreshToken,
            jwtService.accessTokenValiditySeconds(),
            user.getId(), user.getOrganisationId(), user.getRole().name());
    }

    @Transactional
    public AuthResponse signup(String email, String password, String phone) {
        if (appUserRepository.existsByEmailAndDeletedAtIsNull(email)) {
            throw new IllegalStateException("Email already registered");
        }

        // Create organisation first (like OTP flow)
        Organisation org = new Organisation();
        org.setName(email + "'s Fleet");
        org = organisationRepository.save(org);

        UserRole role = UserRole.OWNER;
        AppUser user = new AppUser();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setRole(role);
        user.setPhone(phone);
        user.setName("Owner"); // Required field
        user.setStatus(UserStatus.ACTIVE);
        user.setOrganisationId(org.getId());

        appUserRepository.save(user);

        String accessToken = jwtService.generateAccessToken(
            user.getId(),
            user.getOrganisationId(),
            user.getRole().name()
        );
        String refreshToken = issueRefreshToken(user.getId());

        return new AuthResponse(
            accessToken, refreshToken,
            jwtService.accessTokenValiditySeconds(),
            user.getId(), user.getOrganisationId(), user.getRole().name());
    }

    /**
     * Exchanges a valid refresh token for a new access token AND a new refresh token
     * (rotation). The role in the new access token is read from the database now, not
     * copied from the old token, so a role change takes effect at the next refresh.
     * The accept/reject rules live in RefreshTokenPolicy (and its unit tests).
     */
    @Transactional
    public AuthResponse refresh(String rawRefreshToken) {
        Instant now = Instant.now();

        RefreshToken presented = refreshTokenRepository
                .findByTokenHash(tokenHasher.sha256(rawRefreshToken))
                .orElseThrow(() -> {
                    log.info("Refresh rejected: token not found");
                    return new InvalidRefreshTokenException();
                });

        Verdict verdict = RefreshTokenPolicy.evaluate(
                now, presented.getExpiresAt(), presented.getRevokedAt(), presented.getReplacedAt());
        if (verdict != Verdict.ACCEPT) {
            if (verdict == Verdict.REUSED_AFTER_GRACE) {
                // Worth alerting on once there is monitoring: either a stolen token being
                // replayed, or a client that was offline for longer than the grace window.
                log.warn("Refresh token reused after grace window for user {}", presented.getUserId());
            } else {
                log.info("Refresh rejected: {}", verdict);
            }
            throw new InvalidRefreshTokenException();
        }

        AppUser user = appUserRepository.findById(presented.getUserId())
                .filter(u -> u.getDeletedAt() == null && u.getStatus() != UserStatus.DISABLED)
                .orElseThrow(() -> {
                    log.info("Refresh rejected: user {} missing or disabled", presented.getUserId());
                    return new InvalidRefreshTokenException();
                });

        // Stamp only the first rotation. A retry inside the grace window must not push
        // the window forward, or an old token could be kept alive indefinitely.
        if (presented.getReplacedAt() == null) {
            presented.setReplacedAt(now);
            refreshTokenRepository.save(presented);
        }

        return buildAuthResponse(user);
    }

    /**
     * Revokes the presented refresh token. Idempotent and silent: an unknown or
     * already-revoked token is not an error, and the caller learns nothing either way.
     * Known limit: a token that was rotated out within the last grace window (2 min)
     * is a different row and is not revoked here.
     */
    @Transactional
    public void logout(String rawRefreshToken) {
        refreshTokenRepository.findByTokenHash(tokenHasher.sha256(rawRefreshToken))
                .filter(t -> t.getRevokedAt() == null)
                .ifPresent(t -> {
                    t.setRevokedAt(Instant.now());
                    refreshTokenRepository.save(t);
                });
    }

    private AuthResponse buildAuthResponse(AppUser user) {
        String accessToken = jwtService.generateAccessToken(
            user.getId(),
            user.getOrganisationId(),
            user.getRole().name()
        );
        String refreshToken = issueRefreshToken(user.getId());

        return new AuthResponse(
                accessToken,
                refreshToken,
                jwtService.accessTokenValiditySeconds(),
                user.getId(),
                user.getOrganisationId(),
                user.getRole().name());
    }

    private AppUser createOwnerAndOrganisation(OtpVerifyDto dto, String identifier) {
        Organisation org = new Organisation();
        org.setName(dto.organisationName() != null ? dto.organisationName() : dto.name() + "'s Fleet");
        org = organisationRepository.save(org);

        AppUser user = new AppUser();
        user.setOrganisationId(org.getId());

        // Set email or phone based on identifier format
        if (identifier.contains("@")) {
            user.setEmail(identifier);
            user.setPhone(dto.phone() != null ? dto.phone() : "");
        } else {
            user.setPhone(identifier);
            // Generate placeholder email for phone-only users (email is NOT NULL in DB)
            user.setEmail("phone-" + identifier.replaceAll("[^0-9]", "") + "@yatrik.local");
        }

        user.setName(dto.name() != null ? dto.name() : "Owner");
        user.setRole(UserRole.OWNER);
        user.setStatus(UserStatus.ACTIVE);
        user.setOrganisationId(org.getId());
        // Set a dummy password hash for OTP-only users (password_hash is NOT NULL in DB)
        user.setPasswordHash("OTP_ONLY_ACCOUNT_" + UUID.randomUUID());
        // Set createdBy to organisation's createdBy to maintain audit trail consistency
        if (org.getCreatedBy() != null) {
            user.setCreatedBy(org.getCreatedBy());
        }
        AppUser savedUser = appUserRepository.save(user);
        return savedUser;
    }

    private String issueRefreshToken(UUID userId) {
        byte[] raw = new byte[32];
        random.nextBytes(raw);
        String rawToken = Base64.getUrlEncoder().withoutPadding().encodeToString(raw);

        RefreshToken token = new RefreshToken();
        token.setUserId(userId);
        token.setTokenHash(tokenHasher.sha256(rawToken));
        token.setExpiresAt(Instant.now().plus(REFRESH_TOKEN_DAYS, ChronoUnit.DAYS));
        refreshTokenRepository.save(token);

        return rawToken;
    }
}
