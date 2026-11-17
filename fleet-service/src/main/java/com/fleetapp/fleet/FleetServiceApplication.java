package com.fleetapp.fleet;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.TimeZone;

@SpringBootApplication(scanBasePackages = "com.fleetapp")
public class FleetServiceApplication {

    static {
        // pgjdbc reads TimeZone.getDefault() for Postgres's connection startup packet,
        // and some Windows JVMs resolve IST to the old tz alias "Asia/Calcutta" instead
        // of the current "Asia/Kolkata", which recent Postgres images reject outright.
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Kolkata"));
    }

    public static void main(String[] args) {
        SpringApplication.run(FleetServiceApplication.class, args);
    }
}
