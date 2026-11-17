#!/usr/bin/env powershell
<#
.SYNOPSIS
    Starts the Yatrik Web frontend on port 5173

.DESCRIPTION
    This script starts the Vite development server for the React frontend on port 5173.
    The frontend proxies API requests to the identity-service on port 8095.
#>

param(
    [switch]$InstallDeps = $false
)

Write-Host "Starting Yatrik Web Frontend on port 5173..." -ForegroundColor Green

$projectPath = "D:\Github\Yatrik\web"
Set-Location $projectPath

if ($InstallDeps) {
    Write-Host "Installing dependencies..." -ForegroundColor Yellow
    npm install
    if ($LASTEXITCODE -ne 0) {
        Write-Host "npm install failed!" -ForegroundColor Red
        exit 1
    }
}

Write-Host "Starting Vite dev server..." -ForegroundColor Green

# Start the dev server
npm run dev