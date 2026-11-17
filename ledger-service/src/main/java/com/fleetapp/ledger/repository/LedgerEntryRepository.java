package com.fleetapp.ledger.repository;

import com.fleetapp.ledger.domain.LedgerEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, UUID> {

    List<LedgerEntry> findByPartyIdAndDeletedAtIsNullOrderByEntryDateDesc(UUID partyId);

    /**
     * The total owed across a party's ledger, rebuilt from the entries themselves.
     * This is the source of truth; party_balance is only a cache of it.
     */
    @Query("select coalesce(sum(case when e.entryType = "
            + "com.fleetapp.ledger.domain.EntryType.DEBIT then e.amount else -e.amount end), 0) "
            + "from LedgerEntry e where e.partyId = :partyId and e.deletedAt is null")
    long sumBalanceByPartyId(UUID partyId);

    List<LedgerEntry> findByOrganisationIdAndEntryDateBetweenAndDeletedAtIsNull(
            UUID organisationId, LocalDate from, LocalDate to);
}
