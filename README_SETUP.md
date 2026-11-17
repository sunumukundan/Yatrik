# Yatrik Fleet Platform - Setup Guide

## Overview

This repository contains a complete fleet management platform for Indian transport operators. The system is built as a multi-module Spring Boot 3 project with Java 21, PostgreSQL, and Flyway.

## Architecture

```
fleet-platform/
├── pom.xml                     parent POM: dependency management, plugin management
├── common-lib/                 shared security, tenant base entities, error model
├── gateway/                    Spring Cloud Gateway — the single public entry point (:8080)
├── identity-service/           organisations, users, OTP, tokens          (:8081)
├── fleet-service/              vehicles, drivers, documents, maintenance   (:8082)
├── trip-service/               trips, expenses                              (:8083)
├── ledger-service/             parties, entries, balances, reminders       (:8084)
├── reporting-service/          dashboard reports (aggregates the above)     (:8085)
├── infra/docker/               local Postgres databases
└── scripts/                    end-to-end tests (Java harness + PowerShell)
```

## What it does

| Area | What it covers |
| --- | --- |
| **Identity** | OTP login, refresh-token rotation, logout, organisations, subscriptions |
| **Fleet** | Vehicles, drivers, vehicle and driver documents, compliance (expiry tracking) |
| **Trips** | Trip lifecycle, mileage, per-trip and per-vehicle expenses, profit, driver cash |
| **Maintenance** | Service schedules, work orders, vehicles taken off the road |
| **Ledger** | Customers and suppliers, ledger entries, balances, payment reminders |
| **Reporting** | Utilisation, financials, fuel spend, receivables — for the dashboard |

## Quick Setup Steps

### 1. Prerequisites

- **Java 21+** (JDK 21.0.12+ recommended)
- **Maven 3.9+**
- **Docker** (for databases)

### 2. Start Databases

Each service owns its own database with Flyway migrations:

```bash
cd infra/docker
cp .env.example .env          # then fill in the four passwords
docker compose up -d
```

### 3. Build Services

```bash
mvn clean package
```

### 4. Start All Services

Use the automated deployment script:

```bash
./scripts/deploy-backend.sh --start
```

This will:
- Stop any existing Yatrik services
- Set up environment variables
- Build and start all 6 backend services
- Display status and logs location

### 5. Start Frontend Development

```bash
cd web
npm run dev
```

The frontend will connect to: `http://localhost:8080`

## Testing

### End-to-End Tests

Run the comprehensive test suite:

```bash
# Java harness tests (recommended)
java scripts/e2e/FleetE2E.java /tmp/identity.log      # 48 checks
java scripts/e2e/GatewayRouting.java /tmp/identity.log # 16 checks

# PowerShell tests (alternative)
powershell -ExecutionPolicy Bypass -File scripts\test-auth.ps1
powershell -ExecutionPolicy Bypass -File scripts\test-trips.ps1
```

### Manual API Testing

Test authentication flow:

```bash
# Request OTP
curl -X POST http://localhost:8080/auth/otp/request \
     -H 'Content-Type: application/json' \
     -d '{"phone":"9876543210"}'

# Verify OTP (replace 123456 with actual OTP from logs)
curl -X POST http://localhost:8080/auth/otp/verify \
     -H 'Content-Type: application/json' \
     -d '{"phone":"9876543210","otp":"123456","name":"Owner","organisationName":"Sharma Transport"}'
```

## Environment Variables

Each service requires:

- `JWT_SECRET`: Shared secret for JWT token signing (same across all services)
- `*_DB_PASSWORD`: Database passwords for each service

Example setup:

```bash
export JWT_SECRET=$(openssl rand -base64 48)
export IDENTITY_DB_PASSWORD=local_identity_dev
export FLEET_DB_PASSWORD=local_fleet_dev
export TRIP_DB_PASSWORD=local_trip_dev
export LEDGER_DB_PASSWORD=local_ledger_dev
```

## Configuration

### Database Schema

- **Tenancy**: Every tenant-scoped table carries an `organisation_id`
- **Authentication**: JWT tokens with `org` claim, never from request body
- **Money**: All amounts in `paise` (`BIGINT`), never floating-point
- **Idempotency**: `client_ref` and `LedgerRefs` prevent duplicate processing
- **Soft delete**: Every table has `deleted_at` column for audit trail

### Key Features

1. **Authentication**: OTP-based login with refresh token rotation
2. **Security**: JWT tokens with 30-minute expiry, 2-minute grace window
3. **Multi-tenancy**: Organisation isolation via JWT claims
4. **Offline-first**: Mobile app works offline and syncs later
5. **Audit trail**: Complete history for financial transactions
6. **Responsive**: Mobile-first design

## Technology Stack

- **Backend**: Spring Boot 3, Java 21, PostgreSQL, Flyway
- **Frontend**: React 19, TypeScript, Vite, Tailwind CSS, Redux Query
- **API Gateway**: Spring Cloud Gateway
- **Authentication**: JWT with OTP delivery via Twilio (or logs)
- **Database**: PostgreSQL 16 with Flyway migrations

## File Structure

### Backend Services

- `identity-service/`: Authentication, OTP, token management
- `gateway/`: API gateway and routing
- `fleet-service/`: Vehicles, drivers, maintenance
- `trip-service/`: Trips, expenses, profit calculation
- `ledger-service/`: Customer/supplier ledger, payments
- `reporting-service/`: Analytics and reports

### Frontend

- `web/`: React application with:
  - `src/pages/`: All application screens
  - `src/components/`: UI components
  - `src/auth/`: Authentication context and OTP flow
  - `src/lib/`: API client and utilities

### Infrastructure

- `infra/docker/`: Docker compose for databases
- `scripts/`: E2E tests (Java + PowerShell)

## Troubleshooting

### Common Issues

1. **Port Conflicts**: Services failing to start due to port 8080/8081 conflicts
   - Use `./scripts/deploy-backend.sh --start` to auto-resolve
   - Manually kill Java processes: `ps aux | grep java`

2. **Database Connection Issues**
   - Ensure Docker is running: `docker compose up -d`
   - Check passwords in `infra/docker/.env`

3. **JWT Secret Configuration**
   - Must be identical across all services
   - Use the same `JWT_SECRET` environment variable

4. **Frontend-Backend Connection**
   - Frontend connects to `http://localhost:8080`
   - Check gateway logs: `gateway.log`

### Log Files

- `identity-service.log`: OTP delivery and authentication logs
- `gateway.log`: API gateway routing and request logs
- `fleet-service.log`, `trip-service.log`, etc.: Service-specific logs

## Development Workflow

1. **Make changes** to service code or frontend components
2. **Build services**: `mvn clean package`
3. **Restart services**: `./scripts/deploy-backend.sh --stop && ./scripts/deploy-backend.sh --start`
4. **Test manually** using the API endpoints or frontend
5. **Run e2e tests**: Java harness or PowerShell scripts

## Production Considerations

1. **Security**: Never commit real JWT secrets or database passwords
2. **Environment Variables**: Use environment-specific configuration
3. **Database Backups**: Regular backups of PostgreSQL instances
4. **Monitoring**: Set up health checks and logging
5. **Scaling**: Services are designed for independent scaling

## Getting Help

- Check service logs for detailed error messages
- Refer to individual service documentation
- Review the e2e test scripts for expected behavior
- GitHub issues for known problems and discussions

## License

This project is part of the Yatrik fleet management platform development effort.

---

**Note**: The frontend application is already built and ready. To start development:

1. Start backend services: `./scripts/deploy-backend.sh --start`
2. Start frontend: `cd web && npm run dev`
3. Access the application at: `http://localhost:3000`

The system provides a complete fleet management solution with authentication, vehicle management, trip tracking, maintenance scheduling, financial ledger, and reporting capabilities.