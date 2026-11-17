package com.fleetapp.identity.web.dto;

import java.util.UUID;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        long expiresIn,
        UUID userId,
        UUID organisationId,
        String role
) {}
