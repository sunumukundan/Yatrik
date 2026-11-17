package com.fleetapp.trip.client;

import com.fleetapp.common.security.CurrentUser;
import com.fleetapp.trip.exception.LedgerSyncException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Map;
import java.util.UUID;

/**
 * Synchronous HTTP client for ledger-service. MVP choice: a direct call instead of
 * Kafka events. Swap for an outbox + event consumer once volume or reliability needs it;
 * TripService only depends on postFreightDebit(), so that change stays local to here.
 */
@Component
public class LedgerClient {

    private final RestClient restClient;

    public LedgerClient(@Value("${app.ledger.base-url}") String baseUrl) {
        // Short timeouts: this runs while a user waits on "Complete trip". A hung ledger
        // should fail fast with a retryable error, not freeze the app.
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(3000);
        factory.setReadTimeout(5000);
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(factory)
                .build();
    }

    /**
     * Records the trip's freight as money the customer owes (a DEBIT on their ledger).
     * Idempotent: see LedgerRefs.
     */
    public void postFreightDebit(UUID partyId, UUID tripId, long amountPaise, String note) {
        post(partyId, tripId, "DEBIT", amountPaise, note, LedgerRefs.freightDebit(tripId));
    }

    /** Records money the customer already paid for this trip (a CREDIT on their ledger). */
    public void postCustomerAdvanceCredit(UUID partyId, UUID tripId, long amountPaise, String note) {
        post(partyId, tripId, "CREDIT", amountPaise, note, LedgerRefs.customerAdvanceCredit(tripId));
    }

    /**
     * clientRef is derived from the trip id, so repeating a call (e.g. retrying a completion
     * that failed after this call succeeded) hits ledger-service's unique
     * (organisation_id, client_ref) constraint and comes back as 409, which is treated here
     * as "already recorded", i.e. success.
     */
    private void post(UUID partyId, UUID tripId, String entryType, long amountPaise, String note, UUID clientRef) {
        Map<String, Object> body = Map.of(
                "entryType", entryType,
                "amount", amountPaise,
                "tripId", tripId.toString(),
                "note", note,
                "clientRef", clientRef.toString());

        try {
            restClient.post()
                    .uri("/parties/{partyId}/entries", partyId)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + CurrentUser.get().token())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .toBodilessEntity();
        } catch (HttpClientErrorException.Conflict alreadyRecorded) {
            // This exact entry was already posted: nothing to do.
        } catch (HttpClientErrorException.NotFound noSuchParty) {
            throw new LedgerSyncException(
                    "Customer not found in ledger: " + partyId, HttpStatus.UNPROCESSABLE_ENTITY);
        } catch (HttpClientErrorException rejected) {
            throw new LedgerSyncException(
                    "Ledger rejected the entry (" + rejected.getStatusCode().value() + "); trip not completed",
                    HttpStatus.BAD_GATEWAY);
        } catch (RestClientException unavailable) {
            throw new LedgerSyncException(
                    "Ledger service unavailable; trip not completed, please retry",
                    HttpStatus.BAD_GATEWAY);
        }
    }
}
