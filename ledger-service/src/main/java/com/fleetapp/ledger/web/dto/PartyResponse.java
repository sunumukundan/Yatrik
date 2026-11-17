package com.fleetapp.ledger.web.dto;

import com.fleetapp.ledger.domain.Party;
import com.fleetapp.ledger.domain.PartyType;

import java.util.UUID;

public record PartyResponse(
        UUID id, String name, String phone, PartyType type, String gstNo,
        long openingBalance, long currentBalance
) {
    public static PartyResponse from(Party p, long currentBalance) {
        return new PartyResponse(p.getId(), p.getName(), p.getPhone(), p.getType(),
                p.getGstNo(), p.getOpeningBalance(), currentBalance);
    }
}
