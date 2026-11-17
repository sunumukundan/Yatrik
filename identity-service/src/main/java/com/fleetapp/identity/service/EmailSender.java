package com.fleetapp.identity.service;

/**
 * Delivers a one-time password to an email address.
 *
 * The OTP flow itself never depends on a provider: it hands the code to the sender and
 * keeps the hash-and-verify behaviour. Swapping providers (SMTP today, anything else
 * tomorrow) is therefore a change confined to the sender implementations.
 */
public interface EmailSender {

    /**
     * Sends the OTP to the given email address.
     *
     * @throws IllegalStateException on failure so the caller can decide what to do — an
     *         undelivered OTP simply means the user never receives a code.
     */
    void send(String email, String otp);
}