# PowerShell Startup Scripts for Yatrik Fleet Platform

## Overview

This directory contains PowerShell versions of the Yatrik Fleet Platform deployment scripts that work natively on Windows environments.

## Scripts Available

### Backend Scripts

#### `backend-start.ps1` - Backend Startup (PowerShell)
Starts all Yatrik backend services including the API gateway:
- Checks for Java 21+ and Maven 3.9+ installation
- Builds all Spring Boot services
- Sets up environment variables (JWT secret, database passwords)
- Starts 6 backend services on ports 8080-8085
- Provides detailed status and logging

#### `stop-backend.ps1` - Backend Stop (PowerShell)
Stops all Yatrik backend services:
- Finds and kills all Yatrik Java processes
- Cleans up PID files
- Verifies services are stopped
- Checks port usage

### Frontend Scripts

#### `frontend-start.ps1` - Frontend Startup (PowerShell)
Starts the React frontend with backend integration:
- Checks for Node.js 18+ installation
- Verifies backend availability (ports 8080, 8081)
- Sets up frontend environment configuration
- Starts Vite development server
- Finds available frontend port (default: 3000)

#### `stop-frontend.ps1` - Frontend Stop (PowerShell)
Stops all Yatrik frontend processes:
- Finds and kills frontend processes (Vite, npm dev)
- Checks frontend port usage
- Cleans up temporary files

### Alternative Scripts

#### `backend-start-wrapper.ps1`
Wrapper script for safe execution of backend-start.ps1

#### `backend-start.ps1` (original)
Simple PowerShell version as alternative to bash scripts

## Usage Instructions

### Using PowerShell (Recommended)

1. **Navigate to scripts directory:**
   ```powershell
   cd /d/Github/Yatrik/scripts
   ```

2. **Set execution policy (one-time):**
   ```powershell
   Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser
   ```

3. **Start backend services:**
   ```powershell
   .backend-start.ps1 start
   ```

4. **Start frontend:**
   ```powershell
   .frontend-start.ps1 start
   ```

5. **Stop services:**
   ```powershell
   .stop-backend.ps1 stop
   .stop-frontend.ps1 stop
   ```

6. **Check status:**
   ```powershell
   .backend-start.ps1 status
   .frontend-start.ps1 status
   ```

7. **Show help:**
   ```powershell
   .backend-start.ps1 help
   ```

### Using Bash (WSL/Unix-like environments)

If you have Git for Windows or WSL installed:

```bash
cd /d/Github/Yatrik/scripts
./startup-backend.sh --start-backend
./startup-frontend.sh --start-frontend
./stop-backend.sh --stop
./stop-frontend.sh --stop
```

## System Requirements

### Backend Requirements
- **Java 21+** (JDK 21.0.12+ recommended)
- **Maven 3.9+**
- **Windows 10/11**

### Frontend Requirements
- **Node.js 18+**
- **npm 8+**
- **Windows 10/11**

## Features

### Backend Features
- ✅ Automatic port conflict resolution
- ✅ Service retry mechanisms with detailed logging
- ✅ Environment variable management (JWT, DB passwords)
- ✅ Process cleanup and verification
- ✅ Comprehensive status reporting

### Frontend Features
- ✅ Backend availability checking
- ✅ Automatic port discovery
- ✅ Environment configuration setup
- ✅ Process management and cleanup
- ✅ Status reporting

## Environment Setup

### One-Time Setup

1. **Install required software:**
   - [Java 21+](https://www.oracle.com/java/technologies/javase/jdk21-archive-downloads.html)
   - [Maven 3.9+](https://maven.apache.org/download.cgi)
   - [Node.js 18+](https://nodejs.org/download/)

2. **Set execution policy:**
   ```powershell
   Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser -Force
   ```

3. **Build services (one-time):**
   ```powershell
   cd /d/Github/Yatrik
   mvn clean package -DskipTests
   ```

## Troubleshooting

### Common Issues

#### "Permission denied" errors
1. Set execution policy as shown above
2. Run PowerShell as Administrator if needed
3. Check script file permissions:
   ```powershell
   Get-ChildItem *.ps1 | Select-Object Name, Mode
   ```

#### Missing Java/Maven
- Download and install from official websites
- Ensure Java and Maven are in your PATH

#### Frontend won't start
1. Verify backend is running first
2. Check Node.js version compatibility
3. Ensure npm dependencies are installed:
   ```powershell
   cd /d/Github/Yatrik/web
   npm install
   ```

#### Port conflicts
- Scripts automatically try alternative ports
- Check running processes if conflicts persist:
   ```powershell
   Get-NetTCPConnection | Select-Object LocalPort, OwningProcess
   ```

### Getting Help

#### Script Help
```powershell
# Show help for backend script
.backend-start.ps1 help

# Show help for frontend script  
.frontend-start.ps1 help

# Show help for stop scripts
.stop-backend.ps1 help
.stop-frontend.ps1 help
```

#### Log Files
- **Backend logs:** `*.log` files in scripts directory
- **Frontend logs:** Browser console and terminal output
- **Detailed logs:** Check processes with:
  ```powershell
  Get-Process | Where-Object { $_.ProcessName -eq "java" }
  ```

## Technical Notes

### PowerShell Compatibility
- These scripts use PowerShell 5.1+ compatible syntax
- Should work on Windows 10 and Windows 11
- Avoids advanced features not universally available

### Port Usage
- **Backend services:** 8080 (gateway) through 8085 (reporting)
- **Frontend service:** 3000+ (automatic discovery)
- **Database services:** 5432-5435 (if using Docker)

### File Locations
- **Project root:** `/d/Github/Yatrik`
- **Scripts directory:** `/d/Github/Yatrik/scripts`
- **Web application:** `/d/Github/Yatrik/web`

## Migration from Original Scripts

The original bash scripts (`startup-backend.sh`, `start-frontend.sh`) are still available for environments with:
- WSL (Windows Subsystem for Linux)
- Git Bash
- Other Unix-like environments

For Windows-only environments, use the PowerShell scripts provided in this directory.

## File Summary

| File | Purpose | Platform |
|------|---------|----------|
| `backend-start.ps1` | Backend startup | PowerShell (Windows) |
| `frontend-start.ps1` | Frontend startup | PowerShell (Windows) |
| `stop-backend.ps1` | Backend stop | PowerShell (Windows) |
| `stop-frontend.ps1` | Frontend stop | PowerShell (Windows) |
| `startup-backend.sh` | Backend startup | Bash/Unix |
| `start-frontend.sh` | Frontend startup | Bash/Unix |
| `stop-backend.sh` | Backend stop | Bash/Unix |
| `stop-frontend.sh` | Frontend stop | Bash/Unix |

## Security Notes

- Scripts use only standard PowerShell cmdlets
- No external dependencies beyond Java/Maven/Node.js
- Environment variables are set but not persisted to disk
- Scripts include error handling and validation
- All paths are explicitly specified for security

For production deployments, ensure proper security practices including:
- Regular updates of dependencies
- Secure environment variable management
- Proper logging and monitoring setup
- Regular service health checks