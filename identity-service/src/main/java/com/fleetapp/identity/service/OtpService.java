package com.fleetapp.identity.service;

import com.fleetapp.identity.domain.OtpRequest;
import com.fleetapp.identity.repository.OtpRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Generates and verifies OTPs.
 *
 * Delivery (SMS/WhatsApp/Email) goes through an {@link SmsSender} or {@link EmailSender} — the
 * log-based one while {@code app.sms.provider=log} / {@code app.email.provider=log}, the Twilio
 * one when it is set to {@code twilio}. The OTP itself is never handled in plain text: only its
 * hash is stored, and verification is a BCrypt match. The sender must therefore never see the
 * code after it has been hashed.
 */
@Service
@RequiredArgsConstructor
public class OtpService {

    private static final int OTP_LENGTH = 6;
    private static final int OTP_VALID_MINUTES = 5;
    private static final int MAX_ATTEMPTS = 5;

    private final OtpRequestRepository otpRequestRepository;
    private final SmsSender smsSender;
    private final EmailSender emailSender;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final SecureRandom random = new SecureRandom();

    public void requestOtp(String identifier) {
        String otp = generateOtp();
        String identifierType = isEmail(identifier) ? "email" : "phone";

        OtpRequest request = new OtpRequest();
        request.setIdentifier(identifier);
        request.setIdentifierType(identifierType);
        request.setOtpHash(passwordEncoder.encode(otp));
        request.setExpiresAt(Instant.now().plus(OTP_VALID_MINUTES, ChronoUnit.MINUTES));
        otpRequestRepository.save(request);

        if (identifierType.equals("email")) {
            emailSender.send(identifier, otp);
        } else {
            smsSender.send(identifier, otp);
        }
    }

    /**
     * @return true if the OTP matched and was consumed, false otherwise.
     */
    public boolean verifyOtp(String identifier, String otp) {
        var requestOpt = otpRequestRepository
                .findFirstByIdentifierAndConsumedAtIsNullAndExpiresAtAfterOrderByCreatedAtDesc(identifier, Instant.now());

        if (requestOpt.isEmpty()) {
            return false;
        }

        OtpRequest request = requestOpt.get();

        if (request.getAttempts() >= MAX_ATTEMPTS) {
            return false;
        }

        request.setAttempts(request.getAttempts() + 1);

        boolean matches = passwordEncoder.matches(otp, request.getOtpHash());
        if (matches) {
            request.setConsumedAt(Instant.now());
        }
        otpRequestRepository.save(request);

        return matches;
    }

    private boolean isEmail(String identifier) {
        if (identifier == null) {
            return false;
        }
        // More robust email validation: must contain @ and ., with @ before .
        int atIndex = identifier.indexOf('@');
        int dotIndex = identifier.lastIndexOf('.');
        return atIndex > 0 && dotIndex > atIndex + 1 && dotIndex < identifier.length() - 1;
    }

    private String generateOtp() {
        StringBuilder sb = new StringBuilder(OTP_LENGTH);
        for (int i = 0; i < OTP_LENGTH; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }
}
