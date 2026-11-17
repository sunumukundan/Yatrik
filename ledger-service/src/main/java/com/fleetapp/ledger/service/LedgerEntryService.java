package com.fleetapp.ledger.service;

import com.fleetapp.common.security.CurrentUser;
import com.fleetapp.ledger.domain.EntryType;
import com.fleetapp.ledger.domain.LedgerEntry;
import com.fleetapp.ledger.domain.Party;
import com.fleetapp.ledger.domain.PartyBalance;
import com.fleetapp.ledger.repository.LedgerEntryRepository;
import com.fleetapp.ledger.repository.PartyBalanceRepository;
import com.fleetapp.ledger.web.dto.LedgerEntryCreateDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LedgerEntryService {

    private final LedgerEntryRepository ledgerEntryRepository;
    private final PartyBalanceRepository partyBalanceRepository;
    private final PartyService partyService; // ownership check — confirms party is in this org

    public List<LedgerEntry> list(UUID partyId) {
        partyService.findEntity(partyId); // 404 if not in this org
        return ledgerEntryRepository.findByPartyIdAndDeletedAtIsNullOrderByEntryDateDesc(partyId);
    }

    /**
     * Writes the ledger_entry row AND updates the party_balance cache in the same
     * transaction, so the two never drift apart. If this grows beyond a single-service
     * concern (e.g. balance needs recalculating from a bulk import), add an explicit
     * rebuild-from-entries method rather than trusting this increment alone.
     */
    @Transactional
    public LedgerEntry create(UUID partyId, LedgerEntryCreateDto dto) {
        Party party = partyService.findEntity(partyId);

        LedgerEntry entry = new LedgerEntry();
        entry.setOrganisationId(CurrentUser.get().organisationId());
        entry.setPartyId(partyId);
        entry.setTripId(dto.tripId());
        entry.setEntryType(dto.entryType());
        entry.setAmount(dto.amount());
        if (dto.entryDate() != null) {
            entry.setEntryDate(dto.entryDate());
        }
        entry.setMode(dto.mode());
        entry.setReference(dto.reference());
        entry.setNote(dto.note());
        entry.setClientRef(dto.clientRef());
        entry.setCreatedBy(CurrentUser.get().userId());
        entry = ledgerEntryRepository.save(entry);

        applyToBalance(party, dto.entryType(), dto.amount());

        return entry;
    }

    private void applyToBalance(Party party, EntryType entryType, long amount) {
        PartyBalance balance = partyBalanceRepository.findById(party.getId())
                .orElseGet(() -> {
                    PartyBalance fresh = new PartyBalance();
                    fresh.setPartyId(party.getId());
                    fresh.setOrganisationId(party.getOrganisationId());
                    fresh.setBalance(0);
                    return fresh;
                });

        long delta = entryType == EntryType.DEBIT ? amount : -amount;
        balance.setBalance(balance.getBalance() + delta);
        balance.setLastEntryAt(Instant.now());
        partyBalanceRepository.save(balance);
    }
}
