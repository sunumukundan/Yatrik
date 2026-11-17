package com.fleetapp.reporting.client;

import com.fleetapp.common.security.CurrentUser;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.util.Map;

/**
 * The base HTTP client used to call the other services.
 *
 * Every call carries the caller's own token, because each service verifies the JWT
 * itself and scopes the data by organisation on its own side. The reporting service
 * never sees another tenant's data, because it can only ask on behalf of a token it
 * was given.
 *
 * The timeout is short on purpose: a report that hangs is worse than a report that
 * fails, because the dashboard shows a spinner.
 */
public abstract class ServiceClient {

    protected final RestClient restClient;

    protected ServiceClient(String baseUrl) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(3000);
        factory.setReadTimeout(10000);
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(factory)
                .build();
    }

    /**
     * Issues a GET with the caller's bearer token and the given query parameters.
     */
    protected <T> T get(String uri, Map<String, ?> params, Class<T> type) {
        return restClient.get()
                .uri(uri, params)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + CurrentUser.get().token())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(type);
    }
}
