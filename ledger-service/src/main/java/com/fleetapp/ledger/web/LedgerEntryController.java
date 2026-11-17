package com.fleetapp.ledger.web;

import com.fleetapp.ledger.service.LedgerEntryService;
import com.fleetapp.ledger.web.dto.LedgerEntryCreateDto;
import com.fleetapp.ledger.web.dto.LedgerEntryResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/parties/{partyId}/entries")
@RequiredArgsConstructor
public class LedgerEntryController {

    private final LedgerEntryService ledgerEntryService;

    @GetMapping
    public List<LedgerEntryResponse> list(@PathVariable("partyId") UUID partyId) {
        return ledgerEntryService.list(partyId).stream().map(LedgerEntryResponse::from).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LedgerEntryResponse create(@PathVariable("partyId") UUID partyId,
                                      @Valid @RequestBody LedgerEntryCreateDto dto) {
        return LedgerEntryResponse.from(ledgerEntryService.create(partyId, dto));
    }
}
