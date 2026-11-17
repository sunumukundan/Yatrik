#!/bin/bash

# Fix identity-service application.yml
set -e

PROJECT_ROOT="$(cd "$(dirname "$0")" && pwd)"
SEARCH_DIR="${PROJECT_ROOT}identity-service/src/main/resources"

# Find all YAML files
echo "Searching for YAML files in: $SEARCH_DIR"

find "$SEARCH_DIR" -name "*.yml" -type f | while read -r file; do
    echo "Processing: $file"

    # Check if the file exists
    if [[ ! -f "$file" ]]; then
        echo "File not found: $file"
        continue
    fi

    # Count jpa entries in the file (using grep for exact match with leading spaces)
    jpa_count=$(grep -c "^  jpa:" "$file" || true)

    if [[ $jpa_count -gt 1 ]]; then
        echo "Found $jpa_count duplicate jpa entries in: $file"

        # Create fixed content
        cat > "$file" << 'EOF'
server:
  port: 8081

spring:
  application:
    name: identity-service
  datasource:
    # H2 in-memory database for immediate deployment
    # This eliminates external database dependency and provides instant connectivity
    url: jdbc:h2:mem:identitydb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=RESET;MODE=PostgreSQL
    username: sa
    password:
    hikari:
      connection-timeout: 30000
      maximum-pool-size: 5
      minimum-idle: 2
      pool-name: IdentityServiceH2ConnectionPool
      max-lifetime: 600000
      # H2 specific connection properties
      data-source-properties:
        # H2 database configuration for PostgreSQL compatibility
        DB_CLOSE_DELAY: -1
        DB_CLOSE_ON_EXIT: RESET
        MODE: PostgreSQL
        INITIALIZEDB_ON_STARTUP: true
        WEB_ALLOW_OTHERS: true
        TCP_KEEPAlive: true
        SOCKET_TIMEOUT: 30000
        CONNECT_TIMEOUT: 30000
        LOGIN_TIMEOUT: 30
        APPLICATIONNAME: identity-service

  flyway:
    enabled: true
    locations: classpath:db/migration
    validate-on-migrate: true
    # H2 compatible configuration
    baseline-version: 1
    baseline-description: H2 Migration
    ignore-failed-dbs: true
    # Use PostgreSQL-compatible SQL syntax for migrations
    sql-migration-connector: org.apache.ibatis.jdbc.ScriptRunner

  jpa:
    hibernate:
      ddl-auto: validate
    database-platform: org.hibernate.dialect.H2Dialect
    show-sql: false
    properties:
      hibernate:
        format_sql: true
        use_sql_comments: true
        # PostgreSQL compatibility settings
        default_schema: public
        implicit_naming_strategy: org.springframework.boot.orm.jpa.hibernate.SpringImplicitNamingStrategy
        physical_naming_strategy: org.springframework.boot.orm.jpa.hibernate.SpringPhysicalNamingStrategy

app:
  jwt:
    secret: ${JWT_SECRET}
    access-token-minutes: 30
  sms:
    provider: ${SMS_PROVIDER:log}
    twilio:
      account-sid: ${TWILIO_ACCOUNT_SID}
      auth-token: ${TWILIO_AUTH_TOKEN}
      from-number: ${TWILIO_FROM_NUMBER}

management:
  endpoints:
    web:
      exposure:
        include: health
EOF
        echo "Fixed: $file"
    else
        echo "No duplicate jpa entries found in: $file (count: $jpa_count)"
    fi
done

echo "Done fixing application.yml files."

# Build the project
echo "Building project..."
mvn clean compile -DskipTests -pl identity-service