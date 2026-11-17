# Windows PowerShell Edition of Yatrik Fleet Platform Backend Stop
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

function Stop-AllBackendServices {
    Write-Blue "=== Stopping Yatrik Backend Services ==="

    # Find and stop all Yatrik Java processes
    Write-Blue "Finding Yatrik backend processes..."

    # Method 1: Get all Java processes and check if they're running Yatrik JAR files
    $javaProcesses = Get-Process -ProcessName java -ErrorAction SilentlyContinue
    $yatrikProcesses = @()

    foreach ($process in $javaProcesses) {
        try {
            # Check if this Java process is running a Yatrik JAR file
            $modulePaths = $process.Modules | Where-Object { $_.FileName -like "*target*/*.jar" }
            if ($modulePaths) {
                $yatrikProcesses += $process
                Write-Blue "Found Yatrik process $($process.Id) - $($process.ProcessName)"
            }
        } catch {
            # Skip processes we can't inspect
        }
    }

    # Alternative method: Find processes using Yatrik ports
    if ($yatrikProcesses.Count -eq 0) {
        Write-Blue "Using port-based detection..."
        $portsToCheck = @(8080, 8081, 8082, 8083, 8084, 8085)

        foreach ($port in $portsToCheck) {
            $portProcess = Get-NetTCPConnection -LocalPort $port -ErrorAction SilentlyContinue
            if ($portProcess) {
                $processId = $portProcess.OwningProcess
                if ($processId) {
                    try {
                        $foundProcess = Get-Process -Id $processId -ErrorAction SilentlyContinue
                        if ($foundProcess -and $foundProcess.ProcessName -eq "java") {
                            $yatrikProcesses += $foundProcess
                            Write-Blue "Found Yatrik process $($processId) (using port $port)"
                        }
                    } catch {
                        Write-Warning "Could not access process $processId"
                    }
                }
            }
        }
    }

    # Stop all identified Yatrik processes
    if ($yatrikProcesses.Count -gt 0) {
        Write-Blue "Stopping $($yatrikProcesses.Count) Yatrik processes..."

        foreach ($process in $yatrikProcesses) {
            try {
                Write-Blue "Stopping process $($process.Id) - $($process.ProcessName)"
                $process.Kill()
                $process.WaitForExit(5000)
                Write-Info "✅ Process $($process.Id) stopped"
            } catch {
                Write-Warning "Failed to stop process $($process.Id): $_"
            }
        }

        Write-Info "✅ All Yatrik backend services stopped"
    } else {
        Write-Info "ℹ️ No Yatrik backend Java processes found running"
    }

    # Clean up PID files if they exist
    $pidFiles = @(
        "$script:ProjectRoot\gateway.pid",
        "$script:ProjectRoot\identity-service.pid",
        "$script:ProjectRoot\fleet-service.pid",
        "$script:ProjectRoot\trip-service.pid",
        "$script:ProjectRoot\ledger-service.pid",
        "$script:ProjectRoot\reporting-service.pid"
    )

    foreach ($pidFile in $pidFiles) {
        if (Test-Path $pidFile) {
            Write-Blue "Removing PID file: $pidFile"
            Remove-Item $pidFile -Force
        }
    }

    # Clean up log files
    $logFiles = @(
        "$script:ProjectRoot\gateway.log",
        "$script:ProjectRoot\identity-service.log",
        "$script:ProjectRoot\fleet-service.log",
        "$script:ProjectRoot\trip-service.log",
        "$script:ProjectRoot\ledger-service.log",
        "$script:ProjectRoot\reporting-service.log"
    )

    foreach ($logFile in $logFiles) {
        if (Test-Path $logFile) {
            Write-Blue "Removing log file: $logFile"
            Remove-Item $logFile -Force
        }
    }
}

function Verify-ServicesStopped {
    Write-Blue "Verifying services are stopped..."
    Start-Sleep -Seconds 2

    $stillRunning = 0
    $portsToCheck = @(8080, 8081, 8082, 8083, 8084, 8085)

    foreach ($port in $portsToCheck) {
        $portProcess = Get-NetTCPConnection -LocalPort $port -ErrorAction SilentlyContinue
        if ($portProcess) {
            $stillRunning++
            Write-Warning "Port $port is still in use"
        }
    }

    if ($stillRunning -gt 0) {
        Write-Warning "$stillRunning port(s) are still in use"
        Write-Warning "Some services may still be running"
        return $false
    } else {
        Write-Info "✅ All Yatrik backend services are stopped"
        return $true
    }
}

function Show-Status {
    Write-Blue "=== Yatrik Fleet Platform Backend Status ==="
    Write-Blue ""
    Write-Blue "Backend Services:"
    Write-Blue "  Gateway:            http://localhost:8080"
    Write-Blue "  Identity Service:   http://localhost:8081"
    Write-Blue "  Fleet Service:      http://localhost:8082"
    Write-Blue "  Trip Service:       http://localhost:8083"
    Write-Blue "  Ledger Service:     http://localhost:8084"
    Write-Blue "  Reporting Service:  http://localhost:8085"
    Write-Blue ""
    Write-Blue "Database Services:"
    Write-Blue "  Local PostgreSQL databases (ports 5432-5435):"
    Write-Blue "    - identity-db: port 5432"
    Write-Blue "    - fleet-db: port 5433"
    Write-Blue "    - trip-db: port 5434"
    Write-Blue "    - ledger-db: port 5435"
    Write-Blue ""
    Write-Blue "Log Files (in project root):"
    Write-Blue "  gateway.log"
    Write-Blue "  identity-service.log"
    Write-Blue "  fleet-service.log"
    Write-Blue "  trip-service.log"
    Write-Blue "  ledger-service.log"
    Write-Blue "  reporting-service.log"
}

function Show-Help {
    Write-Host ""
    Write-Blue "=== Yatrik Fleet Platform Backend Stop (PowerShell) ==="
    Write-Host ""
    Write-Blue "USAGE:"
    Write-Blue "  .\stop-backend.ps1 [ACTION]"
    Write-Host ""
    Write-Blue "ACTIONS:"
    Write-Blue "  stop           Stop all Yatrik backend services (default)"
    Write-Blue "  status         Show current service status"
    Write-Blue "  help           Show this help message"
    Write-Host ""
    Write-Blue "EXAMPLES:"
    Write-Blue "  .\stop-backend.ps1 stop"
    Write-Blue "  .\stop-backend.ps1 status"
    Write-Blue "  .\stop-backend.ps1 help"
    Write-Host ""
}

# Main execution
$action = $args[0]
if (-not $action) { $action = "stop" }

# Execute based on action
Switch ($action.ToLower()) {
    "stop" {
        Stop-AllBackendServices
        Verify-ServicesStopped
        Show-Status
    }

    "status" {
        Write-Blue "=== Yatrik Backend Services Status ==="
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