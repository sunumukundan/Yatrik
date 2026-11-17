# Windows PowerShell Edition of Yatrik Fleet Platform Frontend Stop
# Native PowerShell version for Windows environments

# Color codes for output (using PowerShell colors)
$GREEN = "🟢"
$RED = "🔴"
$YELLOW = "🟡"
$BLUE = "🔵"
$NC = ""

function Write-Info    { Write-Host "$GREEN[INFO]    $_" -ForegroundColor Green }
function Write-Error   { Write-Host "$RED[ERROR]   $_" -ForegroundColor Red }
function Write-Warning { Write-Host "$YELLOW[WARNING] $_" -ForegroundColor Yellow }
function Write-Blue    { Write-Host "$BLUE[INFO]     $_" -ForegroundColor Blue }

# Global variables
$script:ProjectRoot = "D:\Github\Yatrik"

function Stop-AllFrontendProcesses {
    Write-Blue "=== Stopping Yatrik Frontend Processes ==="

    # Find and stop all Yatrik frontend processes
    Write-Blue "Finding Yatrik frontend processes..."

    # Method 1: Get all Node.js processes and check if they're running from web directory
    $nodeProcesses = Get-Process -ProcessName node -ErrorAction SilentlyContinue
    $frontendProcesses = @()

    foreach ($process in $nodeProcesses) {
        try {
            # Check if this Node.js process is running from web directory
            $modulePaths = $process.Modules | Where-Object { $_.FileName -like "*Yatrik/web*" }
            if ($modulePaths) {
                $frontendProcesses += $process
                Write-Blue "Found Yatrik frontend process $($process.Id) - $($process.ProcessName)"
            }
        } catch {
            # Skip processes we can't inspect
        }
    }

    # Alternative method: Find processes using frontend ports (Vite default 5173)
    if ($frontendProcesses.Count -eq 0) {
        Write-Blue "Using port-based detection..."
        $portsToCheck = @(5173, 5174, 5175, 5176, 5177, 5178, 3000, 3001, 3002, 3003, 3004, 3005)

        foreach ($port in $portsToCheck) {
            $portProcess = Get-NetTCPConnection -LocalPort $port -ErrorAction SilentlyContinue
            if ($portProcess) {
                $processId = $portProcess.OwningProcess
                if ($processId) {
                    try {
                        $foundProcess = Get-Process -Id $processId -ErrorAction SilentlyContinue
                        if ($foundProcess -and $foundProcess.ProcessName -eq "node") {
                            $frontendProcesses += $foundProcess
                            Write-Blue "Found Yatrik frontend process $processId (using port $port)"
                        }
                    } catch {
                        Write-Warning "Could not access process $processId"
                    }
                }
            }
        }
    }

    # Stop all identified frontend processes
    if ($frontendProcesses.Count -gt 0) {
        Write-Blue "Stopping $($frontendProcesses.Count) Yatrik frontend processes..."

        foreach ($process in $frontendProcesses) {
            try {
                Write-Blue "Stopping frontend process $($process.Id) - $($process.ProcessName)"
                $process.Kill()
                $process.WaitForExit(5000)
                Write-Info "✅ Process $($process.Id) stopped"
            } catch {
                Write-Warning "Failed to stop process $($process.Id): $_"
            }
        }

        Write-Info "✅ All Yatrik frontend processes stopped"
    } else {
        Write-Info "ℹ️ No Yatrik frontend processes found running"
    }

    # Clean up temporary files (but keep .env for next startup)
    Write-Blue "Cleaning up temporary files..."
    $tempFiles = @(
        "$script:ProjectRoot\frontend-build.log",
        "$script:ProjectRoot\vite.log"
    )

    foreach ($tempFile in $tempFiles) {
        if (Test-Path $tempFile) {
            Write-Blue "Removing temporary file: $tempFile"
            Remove-Item $tempFile -Force
        }
    }
}

function Verify-FrontendProcessesStopped {
    Write-Blue "Verifying frontend processes are stopped..."
    Start-Sleep -Seconds 2

    $stillRunning = 0
    $portsToCheck = @(5173, 5174, 5175, 5176, 5177, 5178, 3000, 3001, 3002, 3003, 3004, 3005)

    foreach ($port in $portsToCheck) {
        $portProcess = Get-NetTCPConnection -LocalPort $port -ErrorAction SilentlyContinue
        if ($portProcess) {
            $stillRunning++
            Write-Warning "Port $port is still in use"
        }
    }

    if ($stillRunning -gt 0) {
        Write-Warning "$stillRunning port(s) are still in use"
        Write-Warning "Some frontend processes may still be running"
        return $false
    } else {
        Write-Info "✅ All Yatrik frontend processes are stopped"
        return $true
    }
}

function Show-Status {
    Write-Blue "=== Yatrik Fleet Platform Frontend Status ==="
    Write-Blue ""
    Write-Blue "Frontend:"
    Write-Blue "  Vite Development Server: http://localhost:[VITE_PORT]"
    Write-Blue "  Backend: http://localhost:8080"
    Write-Blue ""
    Write-Blue "Backend Services:"
    Write-Blue "  Gateway:            http://localhost:8080"
    Write-Blue "  Identity Service:   http://localhost:8081"
    Write-Blue "  Fleet Service:      http://localhost:8082"
    Write-Blue "  Trip Service:       http://localhost:8083"
    Write-Blue "  Ledger Service:     http://localhost:8084"
    Write-Blue "  Reporting Service:  http://localhost:8085"
    Write-Blue ""
    Write-Blue "Log Files:"
    Write-Blue "  Frontend: Browser console and terminal output"
    Write-Blue "  Environment: web/.env"
    Write-Blue "  Backend: *.log files in scripts directory"
}

function Show-Help {
    Write-Host ""
    Write-Blue "=== Yatrik Fleet Platform Frontend Stop (PowerShell) ==="
    Write-Host ""
    Write-Blue "USAGE:"
    Write-Blue "  .\stop-frontend.ps1 [ACTION]"
    Write-Host ""
    Write-Blue "ACTIONS:"
    Write-Blue "  stop           Stop all Yatrik frontend processes (default)"
    Write-Blue "  status         Show current frontend service status"
    Write-Blue "  help           Show this help message"
    Write-Host ""
    Write-Blue "EXAMPLES:"
    Write-Blue "  .\stop-frontend.ps1 stop"
    Write-Blue "  .\stop-frontend.ps1 status"
    Write-Blue "  .\stop-frontend.ps1 help"
    Write-Host ""
}

# Main execution
$action = $args[0]
if (-not $action) { $action = "stop" }

# Execute based on action
Switch ($action.ToLower()) {
    "stop" {
        Stop-AllFrontendProcesses
        Verify-FrontendProcessesStopped
        Show-Status
    }

    "status" {
        Write-Blue "=== Yatrik Frontend Services Status ==="
        Show-Status
    }

    "help" {
        Show-Help
    }

    default {
        Write-Error "Unknown action: $action"
        Show-Help
        exit 1
    }
}