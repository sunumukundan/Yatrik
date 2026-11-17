package com.fleetapp.reporting.web.dto;

import java.util.UUID;

/**
 * A party and their outstanding balance, as the reporting service sees it.
 */
public record PartyBalanceResponse(
        UUID id,
        String name,
        String type,
        long openingBalance,
        long currentBalance
) {}
