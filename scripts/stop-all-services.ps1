#!/usr/bin/env powershell
<#
.SYNOPSIS
    Stops all Yatrik Platform services started by start-all-services.ps1

.DESCRIPTION
    This script stops all services by reading PID files and killing the processes.
    Also stops Docker containers for databases.
#>

$ErrorActionPreference = "Continue"

$scriptDir = "D:\Github\Yatrik\scripts"
$pidDir = Join-Path $scriptDir "pids"

Write-Host "Stopping Yatrik Platform - All Services" -ForegroundColor Cyan
Write-Host "=====================================================" -ForegroundColor Cyan

# Stop backend services and frontend
$pidFiles = Get-ChildItem -Path $pidDir -Filter "*.pid" -ErrorAction SilentlyContinue

if ($pidFiles.Count -eq 0) {
    Write-Host "No PID files found. Services may not be running." -ForegroundColor Yellow
} else {
    foreach ($pidFile in $pidFiles) {
        $pid = Get-Content $pidFile -Raw
        $name = $pidFile.BaseName

        if ($pid -match '^\d+$') {
            Write-Host "Stopping $name (PID: $pid)..." -ForegroundColor Yellow
            try {
                Stop-Process -Id $pid -Force -ErrorAction Stop
                Write-Host "   Stopped $name" -ForegroundColor Green
            } catch {
                Write-Host "   Could not stop $name (may already be stopped)" -ForegroundColor Gray
            }
        }
        Remove-Item $pidFile -Force -ErrorAction SilentlyContinue
    }
}

# Also stop docker containers
Write-Host "`nStopping PostgreSQL databases..." -ForegroundColor Yellow
$proc = Start-Process docker-compose -ArgumentList "down" -WorkingDirectory "D:\Github\Yatrik\infra\docker" -Wait -NoNewWindow

Write-Host "`n=====================================================" -ForegroundColor Cyan
Write-Host "All services stopped!" -ForegroundColor Green