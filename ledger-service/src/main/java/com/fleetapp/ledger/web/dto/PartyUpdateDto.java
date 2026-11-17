package com.fleetapp.ledger.web.dto;

/** Every field optional: a null field is left unchanged. */
public record PartyUpdateDto(
        String name,
        String phone,
        String gstNo
) {}
