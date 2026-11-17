# Yatrik Fleet Platform - Deployment Guide

## Overview

This document provides comprehensive deployment instructions for the Yatrik Fleet Platform, covering backend services, frontend application, databases, and the API gateway.

## System Architecture

```
┌─────────────────────────────────────────────────────────────────────┐
│                    Yatrik Fleet Platform                             │
├─────────────────────────────────────────────────────────────────────┤
│ Backend Services (6 microservices) │ API Gateway │ Frontend (React)   │
│ ├─────────────┬─────────────┬─────────────┤                         │
│ │ Identity   │ Gateway     │ Fleet       │                         │
│ │ Service    │            │ Service     │                         │
│ │ (8081)     │ (8080)      │ (8082)      │                         │
│ ├─────────────┼─────────────┼─────────────┤                         │
│ │ Trip Service │ Ledger    │ Reporting  │                         │
│ │ (8083)      │ (8084)     │ (8085)      │                         │
│ └─────────────┴─────────────┴─────────────┘                         │
└─────────────────────────────────────────────────────────────────────┘
```

## Prerequisites

### System Requirements
- **Java 21+** (JDK 21.0.12+ recommended)
- **Maven 3.9+**
- **Node.js 18+** (for frontend)
- **npm 8+** (for frontend)

### Optional but Recommended
- **Docker** (for databases management)
- **IDE** (optional, for development)

## Deployment Options

### Option 1: Quick Start (Recommended for Development)
This option uses the comprehensive startup script that handles all dependencies and configurations.

### Option 2: Traditional Approach
This option follows the original deployment script pattern.

## Quick Start Deployment

### Step 1: Prepare Environment

First, check if you have all required tools installed:

```bash
# Check Java
java -version
# Should return Java 21+

# Check Maven
mvn --version
# Should return Maven 3.9+

# Check Node.js
node --version
# Should return Node.js 18+

# Check npm
npm --version
# Should return npm 8+
```

### Step 2: Start Backend Services

Run the comprehensive backend startup script:

```bash
# Navigate to project root
cd /d/Github/Yatrik

# Start all backend services (including gateway)
./scripts/startup-backend.sh --start-backend
```

**What this script does:**
- Stops any existing Yatrik services
- Builds all backend services
- Sets up environment variables (JWT secret, database passwords, Twilio config)
- Starts all 6 backend services plus the API gateway
- Provides detailed status and logs

### Step 3: Start Frontend

After backend services are running (typically 30-60 seconds), start the frontend:

```bash
# Start frontend
./scripts/startup-frontend.sh --start-frontend
```

**What this script does:**
- Checks if backend gateway is available (port 8080)
- Finds an available port for the frontend (default: 3000)
- Sets up frontend environment configuration
- Starts Vite development server

### Step 4: Access the Application

Once both backend and frontend are running:

- **Frontend URL**: `http://localhost:[VITE_PORT]` (usually 3000)
- **API Gateway**: `http://localhost:8080`
- **Direct Service Access** (for testing):
  - Identity Service: `http://localhost:8081`
  - Gateway: `http://localhost:8080`
  - Fleet Service: `http://localhost:8082`
  - Trip Service: `http://localhost:8083`
  - Ledger Service: `http://localhost:8084`
  - Reporting Service: `http://localhost:8085`

## Advanced Deployment

### Option 2: Traditional Approach

#### Step 1: Start Databases (Optional)

If you want to use Docker-managed databases:

```bash
# Navigate to infra directory
cd /d/Github/Yatrik/infra/docker

# Copy environment template and fill in passwords
cp .env.example .env

# Start databases
export IDENTITY_DB_PASSWORD=local_identity_dev
export FLEET_DB_PASSWORD=local_fleet_dev
export TRIP_DB_PASSWORD=local_trip_dev
export LEDGER_DB_PASSWORD=local_ledger_dev

docker compose up -d
```

#### Step 2: Build Services

```bash
cd /d/Github/Yatrik
mvn clean package -DskipTests
```

#### Step 3: Start Services Manually

```bash
# Set environment variables
export JWT_SECRET="$(openssl rand -base64 48)"
export IDENTITY_DB_PASSWORD=local_identity_dev
export FLEET_DB_PASSWORD=local_fleet_dev
export TRIP_DB_PASSWORD=local_trip_dev
export LEDGER_DB_PASSWORD=local_ledger_dev

# Start services (examples for individual services)
# Identity Service
nohup java -jar identity-service/target/identity-service.jar > identity-service.log 2>&1 &

# Gateway
nohup java -jar gateway/target/gateway.jar > gateway.log 2>&1 &

# Fleet Service
nohup java -jar fleet-service/target/fleet-service.jar > fleet-service.log 2>&1 &
```

## Service Management

### Starting All Services

```bash
# Start everything with one command
cd /d/Github/Yatrik
./scripts/startup-backend.sh --start-all
```

### Stopping Services

```bash
# Stop backend services
cd /d/Github/Yatrik
./scripts/stop-backend.sh --stop

# Stop frontend services
cd /d/Github/Yatrik
./scripts/stop-frontend.sh --stop

# Stop everything
cd /d/Github/Yatrik
./scripts/startup-backend.sh --stop
./scripts/stop-frontend.sh --stop
```

### Checking Service Status

```bash
# Check backend status
cd /d/Github/Yatrik
./scripts/stop-backend.sh --status

# Check frontend status
cd /d/Github/Yatrik
./scripts/stop-frontend.sh --status
```

### Viewing Logs

```bash
# View backend service logs
tail -f *.log

# View specific service logs
tail -f identity-service.log
gateway.log
tail -f fleet-service.log
```

## Environment Configuration

### JWT Secret Management

The JWT secret must be identical across all services. The startup scripts automatically generate and export a consistent secret.

```bash
export JWT_SECRET="$(openssl rand -base64 48)"
```

### Database Configuration

Each service uses its own PostgreSQL database with Flyway migrations:

| Service | Database | Port | User | Password |
|---------|----------|------|------|----------|
| Identity | identity_db | 5432 | identity | local_identity_dev |
| Fleet | fleet_db | 5433 | fleet | local_fleet_dev |
| Trip | trip_db | 5434 | trip | local_trip_dev |
| Ledger | ledger_db | 5435 | ledger | local_ledger_dev |

### Twilio Configuration

For OTP SMS delivery during development (set these in your environment or `.env` file):

```bash
export SMS_PROVIDER="twilio"
export TWILIO_ACCOUNT_SID="<your-twilio-account-sid>"
export TWILIO_AUTH_TOKEN="<your-twilio-auth-token>"
export TWILIO_FROM_NUMBER="+15551234567"
```

## Troubleshooting

### Common Issues

#### Port Conflicts

If services fail to start due to port conflicts:

```bash
# Use the startup script which automatically handles conflicts
./scripts/startup-backend.sh --start-backend

# Or manually kill existing processes
pkill -f "java.*target/.*.jar"
```

#### Database Connection Issues

```bash
# Check if databases are running
docker ps

# Check PostgreSQL connection
psql -h localhost -U identity -d identity_db
```

#### Service Startup Failures

```bash
# Check service logs
tail -f *.log

# Check Java process status
ps aux | grep java
```

### Health Checks

#### Gateway Health

```bash
# Test gateway connectivity
curl -I http://localhost:8080

# Test gateway API
curl -X GET http://localhost:8080/actuator/health
```

#### Service Health

```bash
# Test individual services
curl -I http://localhost:8081
# (Repeat for other service ports)
```

## Development Workflow

### 1. Code Changes

When making changes to services:

```bash
# Build updated services
cd /d/Github/Yatrik
mvn clean package

# Restart services
./scripts/stop-backend.sh --stop
sleep 10
./scripts/startup-backend.sh --start-backend
```

### 2. Frontend Changes

For frontend development:

```bash
# Navigate to web directory
cd web

# Install dependencies if needed
npm install

# Start development server
npm run dev
```

### 3. E2E Testing

Run comprehensive tests:

```bash
# Java harness tests
java scripts/e2e/FleetE2E.java /tmp/identity.log
java scripts/e2e/GatewayRouting.java /tmp/identity.log

# PowerShell tests
powershell -ExecutionPolicy Bypass -File scripts\test-auth.ps1
powershell -ExecutionPolicy Bypass -File scripts\test-trips.ps1
```

## Production Considerations

### Security

- Never commit real JWT secrets or database passwords to version control
- Use environment-specific configuration files
- Regularly rotate JWT secrets
- Secure database credentials

### Performance

- Ensure adequate JVM heap size for production
- Configure proper database connection pooling
- Set up monitoring and logging
- Implement circuit breakers for external services

### Scalability

- Each service is designed for independent scaling
- Use API gateway for routing and load balancing
- Implement rate limiting and throttling
- Configure proper caching strategies

## Files and Directories

### Key Files

- `/scripts/startup-backend.sh` - Comprehensive backend startup
- `/scripts/startup-frontend.sh` - Frontend startup script
- `/scripts/stop-backend.sh` - Backend stop script
- `/scripts/stop-frontend.sh` - Frontend stop script
- `/infra/docker/docker-compose.yml` - Docker database setup
- `/README_SETUP.md` - Original setup guide

### Log Files

- `gateway.log` - API gateway logs
- `identity-service.log` - Identity service logs
- `fleet-service.log` - Fleet service logs
- `trip-service.log` - Trip service logs
- `ledger-service.log` - Ledger service logs
- `reporting-service.log` - Reporting service logs

## Support and Maintenance

### Getting Help

- Check service logs for detailed error messages
- Review e2e test scripts for expected behavior
- GitHub issues for known problems and discussions

### Maintenance Tasks

- Regular log rotation
- Backup database schemas
- Monitor service health
- Update dependencies periodically
- Test deployment procedures regularly

## License

This project is part of the Yatrik fleet management platform development effort.

---

*Generated with Claude Code*