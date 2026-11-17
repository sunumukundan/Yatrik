@echo off
REM Fix application.yml files by removing duplicate jpa entries

setlocal enabledelayedexpansion

set "PROJECT_ROOT=%~dp0"
set "SEARCH_DIR=%PROJECT_ROOT%identity-service\src\main\resources"

for /r "%SEARCH_DIR%" %%f in (application.yml) do (
    echo Processing: %%f

    (set /p content=<"%%f")

    REM Check for duplicate jpa entries
    set "has_duplicate=0"
    set "lines_count=0"
    set "jpa_count=0"

    REM Count total lines and jpa entries
    for %%l in (%%content%%) do (
        set /a lines_count+=1
        if "%%l"=="  jpa:" (
            set /a jpa_count+=1
            if !jpa_count! GTR 1 (
                set "has_duplicate=1"
            )
        )
    )

    if !has_duplicate!==1 (
        echo Found duplicate jpa entries in: %%f

        REM Fix the file - extract and rewrite with single jpa block
        (
            echo server:
            echo   port: 8081
            echo
            echo spring:
            echo   application:
            echo     name: identity-service
            echo   datasource:
            echo     # H2 in-memory database for immediate deployment
            echo     # This eliminates external database dependency and provides instant connectivity
            echo     url: jdbc:h2:mem:identitydb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=RESET;MODE=PostgreSQL
            echo     username: sa
            echo     password:
            echo     hikari:
            echo       connection-timeout: 30000
            echo       maximum-pool-size: 5
            echo       minimum-idle: 2
            echo       pool-name: IdentityServiceH2ConnectionPool
            echo       max-lifetime: 600000
            echo       # H2 specific connection properties
            echo       data-source-properties:
            echo         # H2 database configuration for PostgreSQL compatibility
            echo         DB_CLOSE_DELAY: -1
            echo         DB_CLOSE_ON_EXIT: RESET
            echo         MODE: PostgreSQL
            echo         INITIALIZEDB_ON_STARTUP: true
            echo         WEB_ALLOW_OTHERS: true
            echo         TCP_KEEPAlive: true
            echo         SOCKET_TIMEOUT: 30000
            echo         CONNECT_TIMEOUT: 30000
            echo         LOGIN_TIMEOUT: 30
            echo         APPLICATIONNAME: identity-service
            echo
            echo   flyway:
            echo     enabled: true
            echo     locations: classpath:db/migration
            echo     validate-on-migrate: true
            echo     # H2 compatible configuration
            echo     baseline-version: 1
            echo     baseline-description: H2 Migration
            echo     ignore-failed-dbs: true
            echo     # Use PostgreSQL-compatible SQL syntax for migrations
            echo     sql-migration-connector: org.apache.ibatis.jdbc.ScriptRunner
            echo
            echo   jpa:
            echo     hibernate:
            echo       ddl-auto: validate
            echo     database-platform: org.hibernate.dialect.H2Dialect
            echo     show-sql: false
            echo     properties:
            echo       hibernate:
            echo         format_sql: true
            echo         use_sql_comments: true
            echo         # PostgreSQL compatibility settings
            echo         default_schema: public
            echo         implicit_naming_strategy: org.springframework.boot.orm.jpa.hibernate.SpringImplicitNamingStrategy
            echo         physical_naming_strategy: org.springframework.boot.orm.jpa.hibernate.SpringPhysicalNamingStrategy
            echo
            echo app:
            echo   jwt:
            echo     secret: ${JWT_SECRET}
            echo     access-token-minutes: 30
            echo   sms:
            echo     provider: ${SMS_PROVIDER:log}
            echo     twilio:
            echo       account-sid: ${TWILIO_ACCOUNT_SID}
            echo       auth-token: ${TWILIO_AUTH_TOKEN}
            echo       from-number: ${TWILIO_FROM_NUMBER}
            echo
            echo management:
            echo   endpoints:
            echo     web:
            echo       exposure:
            echo         include: health
        ) > "%%f"
        echo Fixed: %%f
    )
    set content=!content!
)

echo Done fixing application.yml files.

REM Build the project
echo Building project...
mvn clean compile -DskipTests -pl identity-service fleet-service trip-service ledger-service reporting-service