package com.fleetapp.ledger.service;

import com.fleetapp.common.security.CurrentUser;
import com.fleetapp.ledger.domain.Party;
import com.fleetapp.ledger.domain.PartyBalance;
import com.fleetapp.ledger.repository.PartyBalanceRepository;
import com.fleetapp.ledger.repository.PartyRepository;
import com.fleetapp.ledger.web.dto.PartyCreateDto;
import com.fleetapp.ledger.web.dto.PartyResponse;
import com.fleetapp.ledger.web.dto.PartyUpdateDto;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PartyService {

    private final PartyRepository partyRepository;
    private final PartyBalanceRepository partyBalanceRepository;

    public List<PartyResponse> list() {
        UUID orgId = CurrentUser.get().organisationId();
        return partyRepository.findByOrganisationIdAndDeletedAtIsNull(orgId).stream()
                .map(p -> PartyResponse.from(p, currentBalance(p)))
                .toList();
    }

    public PartyResponse get(UUID id) {
        Party party = findEntity(id);
        return PartyResponse.from(party, currentBalance(party));
    }

    /** Package-visible so LedgerEntryService can confirm ownership without a second DTO conversion. */
    Party findEntity(UUID id) {
        return partyRepository.findByIdAndOrganisationIdAndDeletedAtIsNull(id, CurrentUser.get().organisationId())
                .orElseThrow(() -> new EntityNotFoundException("Party not found: " + id));
    }

    @Transactional
    public PartyResponse create(PartyCreateDto dto) {
        Party party = new Party();
        party.setOrganisationId(CurrentUser.get().organisationId());
        party.setName(dto.name());
        party.setPhone(dto.phone());
        party.setType(dto.type());
        party.setGstNo(dto.gstNo());
        party.setOpeningBalance(dto.openingBalance());
        party.setCreatedBy(CurrentUser.get().userId());
        party = partyRepository.save(party);
        return PartyResponse.from(party, dto.openingBalance());
    }

    @Transactional
    public PartyResponse update(UUID id, PartyUpdateDto dto) {
        Party party = findEntity(id);
        if (dto.name() != null) {
            party.setName(dto.name());
        }
        if (dto.phone() != null) {
            party.setPhone(dto.phone());
        }
        if (dto.gstNo() != null) {
            party.setGstNo(dto.gstNo());
        }
        // The type and the opening balance are NOT editable: the type decides which
        // list the party appears in, and the opening balance is the seed the whole
        // ledger is measured from.
        party = partyRepository.save(party);
        return PartyResponse.from(party, currentBalance(party));
    }

    /**
     * openingBalance (fixed, set once) + the cached running total from party_balance
     * (updated by LedgerEntryService on every entry). party_balance has no row until
     * the first entry is posted, hence the empty-is-zero fallback.
     */
    private long currentBalance(Party party) {
        long ledgerTotal = partyBalanceRepository.findById(party.getId())
                .map(PartyBalance::getBalance)
                .orElse(0L);
        return party.getOpeningBalance() + ledgerTotal;
    }
}
