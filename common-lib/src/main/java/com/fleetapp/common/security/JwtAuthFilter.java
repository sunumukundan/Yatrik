package com.fleetapp.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * Reads the bearer token, verifies it and publishes the caller's identity to
 * {@link CurrentUser}.
 *
 * A token that fails verification is dropped rather than rejected here: the request
 * continues unauthenticated and the security configuration answers 401. That keeps the
 * "missing, invalid or expired" path in one place.
 *
 * Deliberately NOT a Spring bean: it is created by CommonSecurityConfig, so a service
 * that needs a different filter (identity-service) can supply its own.
 */
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                     @NonNull HttpServletResponse response,
                                     @NonNull FilterChain chain) throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            try {
                Claims claims = jwtService.parse(token);
                UUID userId = UUID.fromString(claims.getSubject());
                UUID organisationId = UUID.fromString(claims.get("org", String.class));
                String role = claims.get("role", String.class);

                CurrentUser.set(userId, organisationId, role, token);

                var auth = new UsernamePasswordAuthenticationToken(
                        userId, null, List.of(new SimpleGrantedAuthority("ROLE_" + role)));
                SecurityContextHolder.getContext().setAuthentication(auth);
            } catch (JwtException | IllegalArgumentException ex) {
                SecurityContextHolder.clearContext();
            }
        }

        try {
            chain.doFilter(request, response);
        } finally {
            // Always clear — a ThreadLocal on a pooled Tomcat thread leaks across
            // requests otherwise, and the next request would inherit this identity.
            CurrentUser.clear();
        }
    }
}
