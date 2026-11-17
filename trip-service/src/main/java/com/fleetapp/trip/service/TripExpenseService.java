package com.fleetapp.trip.service;

import com.fleetapp.common.security.CurrentUser;
import com.fleetapp.trip.domain.Trip;
import com.fleetapp.trip.domain.TripExpense;
import com.fleetapp.trip.repository.TripExpenseRepository;
import com.fleetapp.trip.web.dto.TripExpenseCreateDto;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TripExpenseService {

    private final TripExpenseRepository tripExpenseRepository;
    private final TripService tripService; // ownership check + closed-trip check

    public List<TripExpense> list(UUID tripId) {
        tripService.get(tripId); // 404 if not in this org
        return tripExpenseRepository.findByTripIdAndDeletedAtIsNullOrderByExpenseDateDesc(tripId);
    }

    @Transactional
    public TripExpense create(UUID tripId, TripExpenseCreateDto dto) {
        Trip trip = tripService.get(tripId);
        tripService.assertNotClosed(trip);

        TripExpense expense = new TripExpense();
        expense.setOrganisationId(CurrentUser.get().organisationId());
        expense.setTripId(tripId);
        expense.setCategory(dto.category());
        expense.setAmount(dto.amount());
        expense.setPaidBy(dto.paidBy());
        if (dto.expenseDate() != null) {
            expense.setExpenseDate(dto.expenseDate());
        }
        expense.setReceiptFileId(dto.receiptFileId());
        expense.setNote(dto.note());
        expense.setClientRef(dto.clientRef());
        expense.setCreatedBy(CurrentUser.get().userId());

        try {
            return tripExpenseRepository.save(expense);
        } catch (DataIntegrityViolationException ex) {
            // Same clientRef already used for this org — the offline mobile sync
            // retried a request that already succeeded. Surfaced as 409 by the error
            // model: the client should treat it as "already saved, do not resubmit".
            throw ex;
        }
    }
}
