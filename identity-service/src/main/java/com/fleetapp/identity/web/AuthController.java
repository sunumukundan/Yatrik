package com.fleetapp.identity.web;

import com.fleetapp.identity.service.AuthService;
import com.fleetapp.identity.web.dto.AuthResponse;
import com.fleetapp.identity.web.dto.LoginRequestDto;
import com.fleetapp.identity.web.dto.OtpRequestDto;
import com.fleetapp.identity.web.dto.OtpVerifyDto;
import com.fleetapp.identity.web.dto.RefreshTokenRequestDto;
import com.fleetapp.identity.web.dto.SignupRequestDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * Requests an OTP for a phone number. The response is deliberately empty: it must
     * not say whether a phone is already registered, or the endpoint would let anyone
     * enumerate the users of the platform.
     */
    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequestDto dto) {
        return authService.login(dto.email(), dto.password());
    }

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse signup(@Valid @RequestBody SignupRequestDto dto) {
        return authService.signup(dto.email(), dto.password(), dto.phone());
    }

    @PostMapping("/otp/request")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void requestOtp(@Valid @RequestBody OtpRequestDto dto) {
        authService.requestOtp(dto.getPreferredIdentifier());
    }

    /**
     * Verifies the OTP and returns the access and refresh tokens.
     *
     * A phone that has never been seen gets a new organisation and an ADMIN account.
     */
    @PostMapping("/otp/verify")
    public AuthResponse verifyOtp(@Valid @RequestBody OtpVerifyDto dto) {
        return authService.verifyAndLogin(dto);
    }

    /** Exchanges a refresh token for a new pair. The old refresh token is rotated out. */
    @PostMapping("/refresh")
    public AuthResponse refresh(@Valid @RequestBody RefreshTokenRequestDto dto) {
        return authService.refresh(dto.refreshToken());
    }

    /**
     * Revokes the refresh token. Always 204, whether or not the token was known:
     * a logout that says "that token was not valid" is a probe oracle.
     */
    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@Valid @RequestBody RefreshTokenRequestDto dto) {
        authService.logout(dto.refreshToken());
    }
}
