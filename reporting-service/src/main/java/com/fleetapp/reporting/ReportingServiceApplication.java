package com.fleetapp.reporting;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;

/**
 * The reporting service.
 *
 * It owns no data: every figure it shows comes from fleet-, trip- or ledger-service
 * over HTTP, using the caller's own token. That is why it has no database of its own,
 * and why the JPA and DataSource autoconfigurations are switched off here — common-lib
 * brings spring-boot-starter-data-jpa along for the shared base entities, and without
 * this exclusion Spring would try to build a DataSource and fail at startup with
 * "Failed to determine a suitable driver class".
 */
@SpringBootApplication(
        scanBasePackages = "com.fleetapp",
        exclude = {DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class})
public class ReportingServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ReportingServiceApplication.class, args);
    }
}
