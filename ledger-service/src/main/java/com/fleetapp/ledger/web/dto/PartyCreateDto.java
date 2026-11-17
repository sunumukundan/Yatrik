package com.fleetapp.ledger.web.dto;

import com.fleetapp.ledger.domain.PartyType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PartyCreateDto(
        @NotBlank String name,
        String phone,
        @NotNull PartyType type,
        String gstNo,
        long openingBalance
) {}
