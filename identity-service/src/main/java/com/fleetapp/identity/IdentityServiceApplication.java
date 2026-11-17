package com.fleetapp.identity;

import com.fleetapp.common.security.CommonSecurityConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

import java.util.TimeZone;

@SpringBootApplication(scanBasePackages = "com.fleetapp")
@ComponentScan(
        basePackages = "com.fleetapp",
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                // Identity-service is the only service that ISSUES tokens, so it needs a
                // JwtService that signs as well as verifies, and a filter that leaves
                // /auth/** open. The shared config in common-lib assumes every service
                // only verifies, so it is excluded here and this service supplies its own.
                classes = CommonSecurityConfig.class))
public class IdentityServiceApplication {

    static {
        // pgjdbc reads TimeZone.getDefault() for Postgres's connection startup packet,
        // and some Windows JVMs resolve IST to the old tz alias "Asia/Calcutta" instead
        // of the current "Asia/Kolkata", which recent Postgres images reject outright.
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Kolkata"));
    }

    public static void main(String[] args) {
        SpringApplication.run(IdentityServiceApplication.class, args);
    }
}
