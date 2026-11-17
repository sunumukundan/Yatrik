package com.fleetapp.common.security;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.time.Instant;

/**
 * The security setup shared by every service that owns data.
 *
 * Two decisions worth keeping:
 *
 *  - Everything requires a valid token. Fleet data is never anonymous.
 *  - A missing, invalid or expired token answers 401, not Spring Security's default
 *    403. The mobile client refreshes its token on 401; a 403 means "authenticated but
 *    not allowed" and must never trigger a refresh. Expired and tampered tokens both
 *    land here, because JwtAuthFilter drops them and the request arrives
 *    unauthenticated.
 */
@Configuration
@RequiredArgsConstructor
public class CommonSecurityConfig {

    @Bean
    public JwtService jwtService(@Value("${app.jwt.secret}") String secret) {
        return new JwtService(secret);
    }

    @Bean
    public JwtAuthFilter jwtAuthFilter(JwtService jwtService) {
        return new JwtAuthFilter(jwtService);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtAuthFilter jwtAuthFilter) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/health", "/error").permitAll()
                        .anyRequest().authenticated())
                .exceptionHandling(ex -> ex.authenticationEntryPoint((request, response, authException) -> {
                    response.setStatus(401);
                    response.setHeader("WWW-Authenticate", "Bearer");
                    response.setContentType("application/json");
                    response.getWriter().write("{\"timestamp\":\"" + Instant.now()
                            + "\",\"error\":\"Missing, invalid or expired access token\"}");
                }))
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
