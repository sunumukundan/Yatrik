#!/usr/bin/env powershell
<#
.SYNOPSIS
    Starts the Yatrik Identity Service backend on port 8095

.DESCRIPTION
    This script starts the identity-service Spring Boot application on port 8095.
    The service provides authentication endpoints including OTP request/verify, signup, login, and token refresh.
#>

param(
    [string]$Profile = "default",
    [switch]$SkipTests = $true
)

Write-Host "Starting Yatrik Identity Service on port 8095..." -ForegroundColor Green

$projectPath = "D:\Github\Yatrik\identity-service"
Set-Location $projectPath

# Build the project first
Write-Host "Building project..." -ForegroundColor Yellow
$buildArgs = @("clean", "package")
if ($SkipTests) { $buildArgs += "-DskipTests" }
mvn @buildArgs

if ($LASTEXITCODE -ne 0) {
    Write-Host "Build failed!" -ForegroundColor Red
    exit 1
}

Write-Host "Build successful. Starting application..." -ForegroundColor Green

# Start the application
$env:SERVER_PORT = "8095"
$env:SPRING_PROFILES_ACTIVE = $Profile

mvn spring-boot:run