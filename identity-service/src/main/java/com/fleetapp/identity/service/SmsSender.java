package com.fleetapp.identity.service;

/**
 * Delivers a one-time password to a phone number.
 *
 * The OTP flow itself never depends on a provider: it hands the code to the sender and
 * keeps the hash-and-verify behaviour. Swapping providers (Twilio today, anything else
 * tomorrow) is therefore a change confined to the sender implementations.
 */
public interface SmsSender {

    /**
     * Sends the OTP to the given phone number.
     *
     * @throws IllegalStateException on failure so the caller can decide what to do — an
     *         undelivered OTP simply means the user never receives a code.
     */
    void send(String phone, String otp);
}
