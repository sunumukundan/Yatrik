package com.fleetapp.reporting.client;

import com.fleetapp.reporting.web.dto.PartyBalanceResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Reads the ledger service's parties and their outstanding balances.
 */
@Component
public class LedgerClient extends ServiceClient {

    public LedgerClient(@Value("${app.ledger.base-url}") String baseUrl) {
        super(baseUrl);
    }

    public List<PartyBalanceResponse> parties() {
        PartyBalanceResponse[] parties = get("/parties", Map.of(), PartyBalanceResponse[].class);
        return parties == null ? List.of() : List.of(parties);
    }
}
