# Yatrik Fleet Management Platform - Analysis Report

## Executive Summary

The Yatrik platform is a well-architected microservices fleet management system built with **Java 21 / Spring Boot 3.3.4 / Spring Cloud 2023.0.3**. It follows a database-per-service pattern with clear separation of concerns. The codebase compiles successfully and all tests pass.

---

## Architecture Overview

### Services (7 modules)

| Service | Port | Database | Purpose |
|---------|------|----------|---------|
| **gateway** | 8080 | None (reactive) | API Gateway - single entry point, routes to downstream services |
| **identity-service** | 8081 | H2 (in-memory) | Authentication, OTP, JWT issuance, organisations, users |
| **fleet-service** | 8082 | PostgreSQL (5433) | Vehicles, drivers, documents, maintenance schedules |
| **trip-service** | 8083 | PostgreSQL (5434) | Trips, expenses, profit calculation |
| **ledger-service** | 8084 | PostgreSQL (5435) | Parties, ledger entries, payment reminders |
| **reporting-service** | 8085 | None | Cross-service reporting (reads from fleet/trip/ledger) |
| **common-lib** | N/A | Shared | JWT verification, tenant context, base entities, exception handling |

### Frontend
- React 19 + Vite + TypeScript + Tailwind CSS
- Port: 5173 (dev), proxies to gateway:8080
- Uses TanStack Query for data fetching

---

## Key Design Decisions

### ✅ Strengths

1. **Database-per-service** - No shared schemas, each service owns its data
2. **JWT at edge** - Gateway doesn't authenticate; each service verifies tokens independently
3. **Shared secret** - Symmetric HS256 JWT (MVP approach, documented for future asymmetric migration)
4. **Flyway migrations** - Schema versioning, `ddl-auto: validate` prevents drift
5. **Timezone handling** - JVM-level `TimeZone.setDefault(Asia/Kolkata)` + JDBC URL options
6. **PostgreSQL enums** - Native enum types with `stringtype=unspecified` for JDBC compatibility
7. **Outbox pattern** - Ledger service has `outbox_event` table for reliable event publishing
8. **Idempotency** - Ledger entries have `client_ref` unique constraint
9. **Soft deletes** - All domain entities have `deleted_at` and version for optimistic locking
10. **Reactive gateway** - Spring Cloud Gateway on WebFlux/Netty, no servlet stack

### ⚠️ Issues & Gaps Found

---

## 1. Gateway Route Configuration Missing

**Severity: HIGH**

The gateway's `application.yml` only routes:
- `/auth/**` → identity-service (8081)
- `/vehicles/**` → fleet-service (8082)
- `/trips/**` → trip-service (8083)
- `/vehicles/*/expenses` → trip-service (8083)

**Missing routes** (used by frontend, reporting-service, fleet-service controllers):
- `/drivers/**` → fleet-service (8082)
- `/maintenance/**` → fleet-service (8082)
- `/work-orders/**` → fleet-service (8082)
- `/compliance/**` → fleet-service (8082)
- `/fleet/summary` → fleet-service (8082)
- `/vehicles/{id}/documents/**` → fleet-service (8082)
- `/parties/**` → ledger-service (8084)
- `/reminders/**` → ledger-service (8084)
- `/reports/**` → reporting-service (8085)

**Impact**: Frontend and reporting-service calls to these endpoints will 404.

---

## 2. Missing Database Passwords in `.env`

**Severity: HIGH**

The infra docker `.env` file has empty passwords:
```bash
IDENTITY_DB_PASSWORD=
FLEET_DB_PASSWORD=
TRIP_DB_PASSWORD=
LEDGER_DB_PASSWORD=
```

Docker Compose will fail to start with: `required variable FLEET_DB_PASSWORD is missing a value`

**Fix**: Copy `.env.example` to `.env` and fill in passwords, or use the root `.env` values.

---

## 3. Identity Service Database Mismatch

**Severity: MEDIUM**

- **infra/docker-compose.yml**: Uses PostgreSQL for identity-db (port 5432)
- **identity-service/application.yml**: Uses H2 in-memory (`jdbc:h2:mem:identitydb`)
- **identity-service/docker-compose.yml**: Uses PostgreSQL with different credentials

**Impact**: Two different database configurations exist. The infra version expects PostgreSQL but the service is configured for H2.

---

## 4. CORS Configuration Missing

**Severity: MEDIUM**

No CORS configuration found in any service. The gateway is reactive (WebFlux) and needs `CorsWebFilter` configuration. Services using servlet stack need `CorsConfigurationSource`.

**Impact**: Frontend at `localhost:5173` calling gateway at `localhost:8080` may face CORS issues in production (though Vite proxy handles it in dev).

---

## 5. Reporting Service Has No Health Endpoint Exposed

**Severity: LOW**

`reporting-service/application.yml` only exposes `health` actuator endpoint. No database (since it has none), but could add `info`, `metrics` for observability.

---

## 6. No Integration/E2E Tests

**Severity: LOW**

- Unit tests only exist for identity-service (10 tests) and trip-service (5 tests)
- No integration tests with Testcontainers
- No contract tests between services
- No E2E tests for the full flow

---

## 7. Gateway Doesn't Forward All Headers

**Severity: LOW**

The gateway routes don't explicitly forward headers like `Authorization`, `X-Request-ID`, etc. Spring Cloud Gateway forwards by default, but it's worth verifying in integration.

---

## 8. Duplicate Configuration in identity-service/application.yml

**Severity: LOW**

The file has significant duplication (e.g., `spring.datasource.hikari` appears twice, `spring.jpa.hibernate.ddl-auto` twice, `management.endpoints.web.exposure.include` twice). Not a functional issue but reduces maintainability.

---

## 9. Missing Routes for Vehicle Documents (Nested Path)

**Severity: MEDIUM**

The gateway has a route for `/vehicles/*/expenses` but not for `/vehicles/{id}/documents/**` which is used by `VehicleDocumentController`.

---

## 10. Frontend Environment Variables Use Placeholders

**Severity: LOW**

`web/.env` has placeholder values for Twilio credentials:
```
REACT_APP_TWILIO_ACCOUNT_SID="placeholder"
REACT_APP_TWILIO_AUTH_TOKEN="placeholder"
```

---

## Service Communication Flow

```
┌─────────────┐     ┌──────────────────┐
│   Browser   │────▶│   Gateway :8080  │
└─────────────┘     └────────┬─────────┘
                              │
        ┌─────────────────────┼─────────────────────┐
        ▼                     ▼                     ▼
┌───────────────┐     ┌───────────────┐     ┌───────────────┐
│Identity :8081 │     │  Fleet :8082  │     │  Trip :8083   │
│   (H2/Postgres)│    │  (PostgreSQL) │     │  (PostgreSQL) │
└───────┬───────┘     └───────┬───────┘     └───────┬───────┘
        │                     │                     │
        │                     │              ┌──────┴──────┐
        │                     │              ▼             ▼
        │              ┌──────┴──────┐  ┌────────┐  ┌────────┐
        │              │ Ledger :8084 │  │Reporting│  │  Other │
        │              │ (PostgreSQL) │  │ :8085  │  │services│
        │              └──────────────┘  └────────┘  └────────┘
        │                     ▲
        └─────────────────────┘
              (JWT verification)
```

---

## Recommended Fixes (Priority Order)

### Immediate (Blockers)
1. **Add missing gateway routes** for all fleet-service, ledger-service, and reporting-service endpoints
2. **Populate infra/docker/.env** with database passwords
3. **Align identity-service database** configuration (choose H2 or PostgreSQL consistently)

### Short-term
4. **Add CORS configuration** to gateway (WebFlux) and servlet services
5. **Fix vehicle documents route** in gateway (`/vehicles/{id}/documents/**`)
6. **Deduplicate identity-service/application.yml**

### Medium-term
7. **Add integration tests** with Testcontainers
8. **Add contract tests** (Pact or Spring Cloud Contract)
9. **Implement asymmetric JWT** (JWKS endpoint on identity-service)
10. **Add observability** (distributed tracing, metrics aggregation)

### Nice to Have
11. **Frontend env** - Replace placeholders with actual values or document they're optional
12. **Gateway resilience** - Add circuit breakers, retries, timeouts
13. **API versioning** - Add version prefix to routes (e.g., `/api/v1/`)

---

## Build & Test Status

```
✅ mvn clean compile -DskipTests    → BUILD SUCCESS
✅ mvn test                         → BUILD SUCCESS (15 tests pass)
```

---

## Service Startup Order (with dependencies)

1. **Databases** (PostgreSQL for fleet/trip/ledger, H2/PostgreSQL for identity)
2. **identity-service** (port 8081) - issues JWT tokens
3. **fleet-service** (port 8082) - depends on identity-service for token verification
4. **trip-service** (port 8083) - depends on identity-service, calls ledger-service
5. **ledger-service** (port 8084) - depends on identity-service
6. **reporting-service** (port 8085) - depends on identity-service, calls fleet/trip/ledger
7. **gateway** (port 8080) - routes to all above
8. **Frontend** (port 5173) - proxies to gateway

---

## Configuration Files Reference

| File | Purpose |
|------|---------|
| `pom.xml` | Parent POM with dependency management |
| `common-lib/pom.xml` | Shared library dependencies |
| `gateway/src/main/resources/application.yml` | Gateway routes (NEEDS FIXES) |
| `identity-service/src/main/resources/application.yml` | Identity config (H2, duplicated) |
| `fleet-service/src/main/resources/application.yml` | Fleet config (PostgreSQL 5433) |
| `trip-service/src/main/resources/application.yml` | Trip config (PostgreSQL 5434) |
| `ledger-service/src/main/resources/application.yml` | Ledger config (PostgreSQL 5435) |
| `reporting-service/src/main/resources/application.yml` | Reporting config (no DB) |
| `infra/docker/docker-compose.yml` | Local databases (NEEDS .env) |
| `infra/docker/.env` | Database passwords (EMPTY - NEEDS VALUES) |
| `web/vite.config.ts` | Frontend dev proxy to gateway |
| `web/.env` | Frontend config (placeholders) |