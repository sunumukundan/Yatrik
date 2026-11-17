package com.fleetapp.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Single entry point for the mobile and web apps. Routing and CORS are configured in
 * application.yml. This service deliberately does NOT authenticate requests: every
 * downstream service verifies the JWT itself and scopes data by the organisation inside
 * it, so the gateway being bypassed (or misconfigured) can never expose another
 * organisation's data. Keep it that way: don't move auth here "to simplify".
 *
 * No database here, so none of the Postgres timezone workaround the other services need.
 */
@SpringBootApplication
public class GatewayApplication {

    public static void main(String[] args) {
        // Configure Spring Boot to use a different port for development
        System.setProperty("server.port", "8080");
        System.setProperty("spring.mvc.pathmatch.matching-strategy", "ant_path_matcher");
        SpringApplication.run(GatewayApplication.class, args);
    }
}
