package com.fleetapp.identity.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record OtpRequestDto(
        @Email String email,
        @Pattern(regexp = "^\\+?[0-9]{10,15}$") String phone
) {
    public OtpRequestDto {
        boolean hasEmail = email != null && !email.isBlank();
        boolean hasPhone = phone != null && !phone.isBlank();
        if (!hasEmail && !hasPhone) {
            throw new IllegalArgumentException("Either email or phone is required");
        }
        if (hasEmail && hasPhone) {
            throw new IllegalArgumentException("Provide either email or phone, not both");
        }
    }

    public String getPreferredIdentifier() {
        return email != null && !email.isBlank() ? email : (phone != null ? phone : "");
    }

    public boolean isEmailAuthentication() {
        return email != null && !email.isBlank();
    }

    public boolean isPhoneAuthentication() {
        return phone != null && !phone.isBlank();
    }
}