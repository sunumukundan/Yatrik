package com.fleetapp.trip;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.TimeZone;

@SpringBootApplication(scanBasePackages = "com.fleetapp")
public class TripServiceApplication {

    static {
        // pgjdbc reads TimeZone.getDefault() before any JDBC URL option is applied, and
        // some Windows JVMs resolve IST to the old "Asia/Calcutta" alias, which recent
        // Postgres images reject outright.
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Kolkata"));
    }

    public static void main(String[] args) {
        SpringApplication.run(TripServiceApplication.class, args);
    }
}
