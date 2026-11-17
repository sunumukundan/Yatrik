package com.fleetapp.ledger.web;

import com.fleetapp.ledger.service.PaymentReminderService;
import com.fleetapp.ledger.web.dto.PaymentReminderCreateDto;
import com.fleetapp.ledger.web.dto.PaymentReminderResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/reminders")
@RequiredArgsConstructor
public class PaymentReminderController {

    private final PaymentReminderService paymentReminderService;

    @GetMapping
    public List<PaymentReminderResponse> list() {
        return paymentReminderService.list().stream().map(PaymentReminderResponse::from).toList();
    }

    @GetMapping("/due-soon")
    public List<PaymentReminderResponse> dueSoon(@RequestParam(defaultValue = "7") int days) {
        return paymentReminderService.dueWithin(days).stream().map(PaymentReminderResponse::from).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PaymentReminderResponse create(@Valid @RequestBody PaymentReminderCreateDto dto) {
        return PaymentReminderResponse.from(paymentReminderService.create(dto));
    }

    @PostMapping("/{id}/sent")
    public PaymentReminderResponse markSent(@PathVariable("id") UUID id,
                                            @RequestParam(defaultValue = "true") boolean success) {
        return PaymentReminderResponse.from(paymentReminderService.markSent(id, success));
    }
}
