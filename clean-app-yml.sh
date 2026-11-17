#!/bin/bash

# Clean application.yml files
set -e

PROJECT_ROOT="$(cd "$(dirname "$0")" && pwd)"
SEARCH_DIR="${PROJECT_ROOT}identity-service/src/main/resources"

# Find and clean YAML files
echo "Searching for YAML files in: $SEARCH_DIR"

find "$SEARCH_DIR" -name "*.yml" -type f | while read -r file; do
    echo "Processing: $file"

    # Create fixed content - simplified H2 configuration
    cat > "$file" << 'EOF'
server:
  port: 8081

spring:
  application:
    name: identity-service
  datasource:
    url: jdbc:h2:mem:identitydb
    username: sa
    hikari:
      connection-timeout: 30000
      maximum-pool-size: 5
      minimum-idle: 2

  flyway:
    enabled: true
    locations: classpath:db/migration
    validate-on-migrate: true
    baseline-version: 1
    baseline-description: H2 Migration
    ignore-failed-dbs: true

  jpa:
    hibernate:
      ddl-auto: validate
    database-platform: org.hibernate.dialect.H2Dialect
    show-sql: false

app:
  jwt:
    secret: ${JWT_SECRET}
    access-token-minutes: 30

management:
  endpoints:
    web:
      exposure:
        include: health
EOF
    echo "Cleaned: $file"
done

echo "Done cleaning application.yml files."

# Build the project
echo "Building project..."
mvn clean compile -DskipTests -pl identity-service