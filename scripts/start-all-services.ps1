#!/usr/bin/env powershell
<#
.SYNOPSIS
    Starts all Yatrik Platform services in background with logging to files

.DESCRIPTION
    This script starts all services in background and writes logs to files under /scripts/logs/
    Services:
    1. PostgreSQL databases (via docker-compose)
    2. Identity Service (port 8095)
    3. Gateway (port 8080)
    4. Fleet Service (port 8082)
    5. Trip Service (port 8083)
    6. Ledger Service (port 8084)
    7. Reporting Service (port 8085)
    8. Frontend (port 5173)

    All logs are written to D:\Github\Yatrik\scripts\logs\
    PID files are written to D:\Github\Yatrik\scripts\pids\
#>

$ErrorActionPreference = "Stop"

$scriptDir = "D:\Github\Yatrik\scripts"
$logDir = Join-Path $scriptDir "logs"
$pidDir = Join-Path $scriptDir "pids"

# Create directories
New-Item -ItemType Directory -Force -Path $logDir | Out-Null
New-Item -ItemType Directory -Force -Path $pidDir | Out-Null

# Clean old PID files
Get-ChildItem -Path $pidDir -Filter "*.pid" -ErrorAction SilentlyContinue | Remove-Item -Force

Write-Host "Starting Yatrik Platform - All Services (Background Mode)" -ForegroundColor Cyan
Write-Host "=====================================================" -ForegroundColor Cyan
Write-Host "Logs: $logDir" -ForegroundColor Gray
Write-Host "PIDs: $pidDir" -ForegroundColor Gray

# ============================================================
# 1. Start PostgreSQL databases
# ============================================================
Write-Host "`n1. Starting PostgreSQL databases..." -ForegroundColor Yellow
$dockerLog = Join-Path $logDir "docker-compose.log"
$dockerErrLog = Join-Path $logDir "docker-compose-err.log"
$proc = Start-Process docker-compose -ArgumentList "up -d" -WorkingDirectory "D:\Github\Yatrik\infra\docker" -Wait -RedirectStandardOutput $dockerLog -RedirectStandardError $dockerErrLog -NoNewWindow

Write-Host "   Waiting for databases to be healthy..." -ForegroundColor Gray
Start-Sleep -Seconds 10

# Verify databases
$healthy = 0
for ($i = 0; $i -lt 30; $i++) {
    $result = docker ps --filter "health=healthy" --format "{{.Names}}" 2>$null
    $healthy = ($result -split "`n" | Where-Object { $_ -like "fleet-*-db" }).Count
    if ($healthy -eq 4) { break }
    Start-Sleep -Seconds 2
}
Write-Host "   Databases healthy: $healthy/4" -ForegroundColor Green

# ============================================================
# 2. Start backend services
# ============================================================
Write-Host "`n2. Starting backend services..." -ForegroundColor Yellow

$envVars = @{
    JWT_SECRET = "dev-secret-key-change-in-production-min-32-chars-long"
    IDENTITY_DB_PASSWORD = "local_identity_dev"
    FLEET_DB_PASSWORD = "local_fleet_dev"
    TRIP_DB_PASSWORD = "local_trip_dev"
    LEDGER_DB_PASSWORD = "local_ledger_dev"
}

$services = @(
    @{ Name = "Identity Service"; Path = "D:\Github\Yatrik\identity-service"; Port = 8095; Profile = "default" },
    @{ Name = "Gateway"; Path = "D:\Github\Yatrik\gateway"; Port = 8080; Profile = "default" },
    @{ Name = "Fleet Service"; Path = "D:\Github\Yatrik\fleet-service"; Port = 8082; Profile = "default" },
    @{ Name = "Trip Service"; Path = "D:\Github\Yatrik\trip-service"; Port = 8083; Profile = "default" },
    @{ Name = "Ledger Service"; Path = "D:\Github\Yatrik\ledger-service"; Port = 8084; Profile = "default" },
    @{ Name = "Reporting Service"; Path = "D:\Github\Yatrik\reporting-service"; Port = 8085; Profile = "default" }
)

$allStarted = $true

foreach ($svc in $services) {
    $logFile = Join-Path $logDir "$($svc.Name.Replace(' ', '-')).log"
    $pidFile = Join-Path $pidDir "$($svc.Name.Replace(' ', '-')).pid"

    Write-Host "`n   Starting $($svc.Name) on port $($svc.Port)..." -ForegroundColor Green
    Write-Host "   Log: $logFile" -ForegroundColor Gray

    # Build first
    $buildLog = Join-Path $logDir "$($svc.Name.Replace(' ', '-'))-build.log"
    Write-Host "   Building..." -ForegroundColor Gray
    Set-Location $svc.Path
    $buildResult = mvn clean package -DskipTests *> $buildLog 2>&1

    if ($LASTEXITCODE -ne 0) {
        Write-Host "   ✗ Build failed! Check $buildLog" -ForegroundColor Red
        $allStarted = $false
        continue
    }
    Write-Host "   Build successful" -ForegroundColor Green

    # Start in background
    $env:SERVER_PORT = $svc.Port.ToString()
    $env:SPRING_PROFILES_ACTIVE = $svc.Profile
    $env:JWT_SECRET = $envVars.JWT_SECRET
    $env:IDENTITY_DB_PASSWORD = $envVars.IDENTITY_DB_PASSWORD
    $env:FLEET_DB_PASSWORD = $envVars.FLEET_DB_PASSWORD
    $env:TRIP_DB_PASSWORD = $envVars.TRIP_DB_PASSWORD
    $env:LEDGER_DB_PASSWORD = $envVars.LEDGER_DB_PASSWORD

    # Use Start-Process with -PassThru to get PID, redirect output to log file
    $process = Start-Process powershell -ArgumentList @(
        "-NoExit",
        "-Command",
        "& { mvn spring-boot:run *>> '$logFile' 2>&1 }"
    ) -WorkingDirectory $svc.Path -PassThru -WindowStyle Hidden

    $process.Id | Out-File -FilePath $pidFile -Encoding utf8
    Write-Host "   Started (PID: $($process.Id))" -ForegroundColor Green

    # Small delay between services
    Start-Sleep -Seconds 3
}

# ============================================================
# 3. Start Frontend
# ============================================================
Write-Host "`n3. Starting Frontend on port 5173..." -ForegroundColor Yellow
$frontendLog = Join-Path $logDir "frontend.log"
$frontendPid = Join-Path $pidDir "frontend.pid"

$frontendProcess = Start-Process powershell -ArgumentList @(
    "-NoExit",
    "-Command",
    "& { npm run dev *>> '$frontendLog' 2>&1 }"
) -WorkingDirectory "D:\Github\Yatrik\web" -PassThru -WindowStyle Hidden

$frontendProcess.Id | Out-File -FilePath $frontendPid -Encoding utf8
Write-Host "   Frontend started (PID: $($frontendProcess.Id))" -ForegroundColor Green
Write-Host "   Log: $frontendLog" -ForegroundColor Gray

# ============================================================
# Summary
# ============================================================
Write-Host "`n=====================================================" -ForegroundColor Cyan
if ($allStarted) {
    Write-Host "All backend services started!" -ForegroundColor Green
} else {
    Write-Host "Some services failed to build - check logs" -ForegroundColor Yellow
}
Write-Host ""
Write-Host "Service URLs:" -ForegroundColor Cyan
Write-Host "  Frontend:          http://localhost:5173"
Write-Host "  Gateway (API):     http://localhost:8080"
Write-Host "  Identity Service:  http://localhost:8095"
Write-Host "  Fleet Service:     http://localhost:8082"
Write-Host "  Trip Service:      http://localhost:8083"
Write-Host "  Ledger Service:    http://localhost:8084"
Write-Host "  Reporting Service: http://localhost:8085"
Write-Host ""
Write-Host "Logs directory: $logDir" -ForegroundColor Gray
Write-Host "PID directory:  $pidDir" -ForegroundColor Gray
Write-Host ""
Write-Host "To tail a log:" -ForegroundColor Gray
Write-Host "  Get-Content $logDir\identity-service.log -Wait -Tail 20" -ForegroundColor Gray
Write-Host ""
Write-Host "To stop all services:" -ForegroundColor Gray
Write-Host "  .\scripts\stop-all-services.ps1" -ForegroundColor Gray