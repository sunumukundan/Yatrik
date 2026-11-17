package com.fleetapp.identity.service;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * Dev/test fallback sender.
 *
 * Logs the OTP instead of delivering it, so the platform can be exercised end-to-end
 * without an email provider. Selected when {@code app.email.provider} is "log".
 *
 * DEV ONLY: this provider must never survive into production — it would print every OTP
 * to the log.
 */
@Service
@ConditionalOnProperty(name = "app.email.provider", havingValue = "log")
public class LogEmailSender implements EmailSender {

    private static final int OTP_VALID_MINUTES = 5;

    @Override
    public void send(String email, String otp) {
        System.out.printf("[DEV ONLY] OTP for %s is %s (expires in %d min)%n",
                email, otp, OTP_VALID_MINUTES);
    }
}