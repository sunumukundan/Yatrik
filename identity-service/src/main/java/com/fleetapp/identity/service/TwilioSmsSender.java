package com.fleetapp.identity.service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * Sends OTPs over SMS through the Twilio REST API.
 *
 * Configured via {@code app.sms.twilio.*} (env-overridable as {@code TWILIO_ACCOUNT_SID},
 * {@code TWILIO_AUTH_TOKEN} and {@code TWILIO_FROM_NUMBER}). No extra Spring dependency is
 * needed — the send is a single HTTPS call via the JDK's HttpClient, which keeps the
 * service's dependency footprint unchanged.
 */
@Service
@ConditionalOnProperty(name = "app.sms.provider", havingValue = "twilio")
public class TwilioSmsSender implements SmsSender {

    private static final String TWILIO_MESSAGES_URL =
            "https://api.twilio.com/2010-04-01/Accounts/%s/Messages.json";

    private final String accountSid;
    private final String authToken;
    private final String fromNumber;
    private final HttpClient httpClient;

    public TwilioSmsSender(
            @Value("${app.sms.twilio.account-sid}") String accountSid,
            @Value("${app.sms.twilio.auth-token}") String authToken,
            @Value("${app.sms.twilio.from-number}") String fromNumber) {
        this.accountSid = accountSid;
        this.authToken = authToken;
        this.fromNumber = fromNumber;
        this.httpClient = HttpClient.newBuilder().build();
    }

    @Override
    public void send(String phone, String otp) {
        if (accountSid.isBlank() || authToken.isBlank() || fromNumber.isBlank()) {
            throw new IllegalStateException(
                    "Twilio SMS sender is not configured: set app.sms.twilio.account-sid, "
                    + "app.sms.twilio.auth-token and app.sms.twilio.from-number");
        }

        String body = "Your Yatrik verification code is " + otp + ". It expires in 5 minutes.";

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(TWILIO_MESSAGES_URL.formatted(accountSid)))
                    .header("Authorization", "Basic " + base64Of(accountSid + ":" + authToken))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(BodyPublishers.ofString(
                            "To=" + e164(phone) + "&From=" + e164(fromNumber) + "&Body="
                                    + URLEncoder.encode(body, StandardCharsets.UTF_8)))
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                return;
            }
            throw new IllegalStateException("Twilio SMS send failed: HTTP "
                    + response.statusCode() + " " + response.body());
        } catch (IOException ex) {
            throw new IllegalStateException("Twilio SMS send failed: " + ex.getMessage(), ex);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Twilio SMS send interrupted", ex);
        }
    }

    /**
     * Normalises any accepted phone number to E.164 format.
     * Supports numbers with or without country code. If no country code is provided,
     * defaults to India (+91) for backward compatibility.
     */
    private String e164(String phone) {
        String cleaned = phone.replaceAll("[^0-9+]", "");

        if (cleaned.startsWith("+")) {
            // Already has country code
            return cleaned;
        }

        // Check if it looks like it has a country code (starts with common country codes)
        if (cleaned.length() > 10 && (cleaned.startsWith("1") || cleaned.startsWith("44") ||
                cleaned.startsWith("91") || cleaned.startsWith("61") || cleaned.startsWith("86"))) {
            return "+" + cleaned;
        }

        // Default to India (+91) for backward compatibility
        return "+91" + cleaned.replaceFirst("^0+", "");
    }

    private String base64Of(String value) {
        return Base64.getEncoder().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }
}
