# Yatrik Fleet Platform - Complete Deployment Guide

## Overview

This repository contains a comprehensive fleet management platform for Indian transport operators. The system is built as a multi-module Spring Boot 3 project with Java 21, PostgreSQL, and Flyway.

## Quick Deployment Options

### Option 1: Bash/Unix-like Environments (Recommended for Linux/WSL)

#### Prerequisites
1. Java 21+ (JDK 21.0.12+)
2. Maven 3.9+
3. Node.js 18+
4. npm 8+

#### Deployment Steps
```bash
# Navigate to project root
cd /d/Github/Yatrik

# Build services (one-time)
mvn clean package -DskipTests

# Navigate to scripts directory
cd scripts

# Make all scripts executable
chmod +x *.sh

# Start backend services (includes API gateway)
./startup-backend.sh --start-backend

# Start frontend (after backend is ready)
./start-frontend.sh --start-frontend
```

### Option 2: PowerShell (Windows)

#### Prerequisites
1. Java 21+ (JDK 21.0.12+)
2. Maven 3.9+
3. Node.js 18+
4. npm 8+

#### Deployment Steps
```powershell
# Set execution policy (one-time)
Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser -Force

# Navigate to scripts directory
cd /d/Github/Yatrik/scripts

# Start backend services
.frontend-start.ps1 start

# Start frontend (after backend is ready)
.frontend-start.ps1 start
```

### Option 3: Manual Docker Setup (Optional)

If you want Docker-managed databases:
```bash
# Navigate to infra directory
cd /d/Github/Yatrik/infra/docker

# Copy .env.example to .env and fill in passwords
cp .env.example .env

# Start databases
export IDENTITY_DB_PASSWORD=local_identity_dev
export FLEET_DB_PASSWORD=local_fleet_dev
export TRIP_DB_PASSWORD=local_trip_dev
export LEDGER_DB_PASSWORD=local_ledger_dev

docker compose up -d
```

## Service Architecture

### Backend Services (6 Microservices + API Gateway)
| Service | Port | Purpose |
|---------|------|---------|
| Gateway | 8080 | API Gateway & Routing |
| Identity Service | 8081 | Authentication & Users |
| Fleet Service | 8082 | Vehicles & Drivers |
| Trip Service | 8083 | Trips & Expenses |
| Ledger Service | 8084 | Financial Ledger |
| Reporting Service | 8085 | Analytics & Reports |

### Frontend
- **Technology:** React 19 + TypeScript + Vite + Tailwind CSS
- **Port:** 3000+ (automatic discovery)
- **Backend Connection:** `http://localhost:8080`

## Environment Variables

### Backend Environment Variables
```bash
export JWT_SECRET="$(openssl rand -base64 48)"
export IDENTITY_DB_PASSWORD="local_identity_dev"
export FLEET_DB_PASSWORD="local_fleet_dev"
export TRIP_DB_PASSWORD="local_trip_dev"
export LEDGER_DB_PASSWORD="local_ledger_dev"
export SMS_PROVIDER="twilio"
export TWILIO_ACCOUNT_SID="<your-twilio-account-sid>"
export TWILIO_AUTH_TOKEN="<your-twilio-auth-token>"
export TWILIO_FROM_NUMBER="+15551234567"
```

## Service Management

### Start Services
```bash
# Start everything with one command
cd /d/Github/Yatrik/scripts
chmod +x *.sh
./startup-backend.sh --start-backend
./start-frontend.sh --start-frontend
```

```powershell
# Start backend services
cd /d/Github/Yatrik/scripts
.frontend-start.ps1 start

# Start frontend services
.frontend-start.ps1 start
```

### Stop Services
```bash
# Stop all services
cd /d/Github/Yatrik/scripts
chmod +x *.sh
./stop-backend.sh --stop
./stop-frontend.sh --stop
```

```powershell
# Stop backend services
cd /d/Github/Yatrik/scripts
.frontend-start.ps1 stop

# Stop frontend services
.frontend-start.ps1 stop
```

### Check Status
```bash
# Check service status
cd /d/Github/Yatrik/scripts
./stop-backend.sh --status
./stop-frontend.sh --status
```

```powershell
# Check service status
cd /d/Github/Yatrik/scripts
.frontend-start.ps1 status
```

## Log Files

### Backend Logs
- `gateway.log` - API gateway logs
- `identity-service.log` - Identity service logs
- `fleet-service.log` - Fleet service logs
- `trip-service.log` - Trip service logs
- `ledger-service.log` - Ledger service logs
- `reporting-service.log` - Reporting service logs

### Frontend Logs
- Browser console
- Terminal output where frontend is running
- `web/.env` - Frontend environment configuration

## Access URLs

### Application Access
- **Frontend:** `http://localhost:[VITE_PORT]` (usually 3000)
- **API Gateway:** `http://localhost:8080`

### Direct Service Access (for testing)
- **Gateway:** `http://localhost:8080`
- **Identity Service:** `http://localhost:8081`
- **Fleet Service:** `http://localhost:8082`
- **Trip Service:** `http://localhost:8083`
- **Ledger Service:** `http://localhost:8084`
- **Reporting Service:** `http://localhost:8085`

## Testing

### End-to-End Tests
```bash
# Java harness tests (recommended)
cd /d/Github/Yatrik
java scripts/e2e/FleetE2E.java /tmp/identity.log      # 48 checks
java scripts/e2e/GatewayRouting.java /tmp/identity.log # 16 checks

# PowerShell tests (alternative)
powershell -ExecutionPolicy Bypass -File scripts\test-auth.ps1
powershell -ExecutionPolicy Bypass -File scripts\test-trips.ps1
```

### Manual API Testing
```bash
# Request OTP
curl -X POST http://localhost:8080/auth/otp/request \
     -H 'Content-Type: application/json' \
     -d '{"phone":"9876543210"}'

# Verify OTP (replace 123456 with actual OTP)
curl -X POST http://localhost:8080/auth/otp/verify \
     -H 'Content-Type: application/json' \
     -d '{"phone":"9876543210","otp":"123456","name":"Owner","organisationName":"Sharma Transport"}'
```

## Troubleshooting

### Common Issues

#### Service fails to start
**Solution:**
1. Check port conflicts
2. Verify Java and Maven installation
3. Check logs for errors:
   ```bash
   tail -f *.log
   ```

#### Frontend can't connect to backend
**Solution:**
1. Ensure backend services are running first
2. Check backend accessibility:
   ```bash
   curl http://localhost:8080
   ```

#### Port conflicts
**Solution:**
1. Use deployment scripts that handle conflicts automatically
2. Manually kill conflicting processes:
   ```bash
   pkill -f "java.*target.*jar"
   ```

### Debugging Commands
```bash
# Check running Java processes
ps aux | grep java | grep -v grep

# Check running Node processes
ps aux | grep node | grep -v grep

# Check network connections
netstat -tuln | grep :8080
netstat -tuln | grep :3000

# Check process by port
lsof -i :8080
lsof -i :3000
```

## Production Considerations

### Security
- Never commit real JWT secrets or database passwords
- Use environment-specific configuration files
- Regular JWT secret rotation
- Secure database credential management

### Performance
- Ensure adequate JVM heap size
- Configure proper database connection pooling
- Set up monitoring and logging
- Implement circuit breakers for external services

### Scalability
- Each service is designed for independent scaling
- Use API gateway for routing and load balancing
- Implement rate limiting and throttling
- Configure proper caching strategies

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

## Quick Start Summary

### For Linux/WSL Users
```bash
# Quick deployment
mkdir -p /tmp/yatrik-deployment && cd /tmp/yatrik-deployment
cp -r /d/Github/Yatrik .
cd Yatrik/scripts
chmod +x *.sh
./startup-backend.sh --start-backend
./start-frontend.sh --start-frontend
```

### For Windows PowerShell Users
```powershell
# Quick deployment
mkdir C:\temp\yatrik-deployment
copy /d /Y "\d\Github\Yatrik\*" "C:\temp\yatrik-deployment" /E /I
cd "C:\temp\yatrik-deployment\Yatrik\scripts"
Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser -Force
.frontend-start.ps1 start
```

## License

This project is part of the Yatrik fleet management platform development effort.

---

**Note:** The frontend application is already built and ready. The deployment scripts provide a comprehensive solution for getting the entire Yatrik Fleet Platform up and running quickly.

Generated with Claude Code