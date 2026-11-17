package com.fleetapp.ledger.web;

import com.fleetapp.ledger.service.PartyService;
import com.fleetapp.ledger.web.dto.PartyCreateDto;
import com.fleetapp.ledger.web.dto.PartyResponse;
import com.fleetapp.ledger.web.dto.PartyUpdateDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/parties")
@RequiredArgsConstructor
public class PartyController {

    private final PartyService partyService;

    @GetMapping
    public List<PartyResponse> list() {
        return partyService.list();
    }

    @GetMapping("/{id}")
    public PartyResponse get(@PathVariable("id") UUID id) {
        return partyService.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PartyResponse create(@Valid @RequestBody PartyCreateDto dto) {
        return partyService.create(dto);
    }

    @PutMapping("/{id}")
    public PartyResponse update(@PathVariable("id") UUID id,
                                @Valid @RequestBody PartyUpdateDto dto) {
        return partyService.update(id, dto);
    }
}
