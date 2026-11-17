#!/bin/bash

# Yatrik Fleet Platform Backend Stop Script
# Stops all backend services including gateway and databases
# Works on Linux/macOS and Windows (Git Bash/WSL)

# Get the script directory and project root
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$SCRIPT_DIR/.."

# Color codes for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

print_info() { echo -e "${GREEN}[INFO]${NC} $1"; }
print_error() { echo -e "${RED}[ERROR]${NC} $1"; }
print_warning() { echo -e "${YELLOW}[WARNING]${NC} $1"; }
print_blue() { echo -e "${BLUE}[BLUE]${NC} $1"; }

# Function to stop all Yatrik backend services
stop_all_services() {
    print_info "=== Stopping Yatrik Backend Services ==="

    # Method 1: Try lsof (Linux/macOS)
    if command -v lsof &> /dev/null; then
        print_info "Finding Yatrik backend processes using lsof..."
        for port in 8080 8081 8082 8083 8084 8085; do
            pids=$(lsof -ti :$port 2>/dev/null || true)
            for pid in $pids; do
                print_info "Stopping process $pid on port $port..."
                kill $pid 2>/dev/null || true
                sleep 1
                kill -9 $pid 2>/dev/null || true
            done
        done
    # Method 2: Try netstat with PID (Linux)
    elif command -v netstat &> /dev/null && netstat -tulnp 2>/dev/null | head -1 | grep -q "PID"; then
        print_info "Finding Yatrik backend processes using netstat..."
        for port in 8080 8081 8082 8083 8084 8085; do
            pids=$(netstat -tulnp 2>/dev/null | grep ":$port " | awk '{print $7}' | cut -d'/' -f1 | grep -E '^[0-9]+$')
            for pid in $pids; do
                print_info "Stopping process $pid on port $port..."
                kill $pid 2>/dev/null || true
                sleep 1
                kill -9 $pid 2>/dev/null || true
            done
        done
    # Method 3: Use PowerShell on Windows (Git Bash/WSL)
    elif command -v powershell.exe &> /dev/null; then
        print_info "Finding Yatrik backend processes using PowerShell..."
        powershell.exe -Command "
            Get-Process java -ErrorAction SilentlyContinue | ForEach-Object {
                \$pid = \$_.Id
                Stop-Process -Id \$pid -Force -ErrorAction SilentlyContinue
                Write-Host \"Stopped Java process \$pid\"
            }
        " 2>/dev/null
    # Method 4: Fallback - find java jar processes (Unix-like)
    else
        print_info "Finding Yatrik backend processes using ps..."
        pids=$(ps aux | grep "java.*target/.*.jar" | grep -v grep | awk '{print $2}')
        if [ -n "$pids" ]; then
            print_info "Found processes to stop: $pids"
            for pid in $pids; do
                print_info "Stopping process $pid (Yatrik backend service)..."
                kill $pid 2>/dev/null || true
                sleep 1
                kill -9 $pid 2>/dev/null || true
            done
        else
            print_info "No Yatrik backend Java processes found running"
        fi
    fi

    # Clean up PID files if they exist
    cd "$PROJECT_ROOT"
    for pid_file in gateway.pid identity-service.pid fleet-service.pid trip-service.pid ledger-service.pid reporting-service.pid; do
        if [ -f "$pid_file" ]; then
            print_info "Removing $pid_file file..."
            rm -f "$pid_file"
        fi
    done

    # Clean up log files
    print_info "Cleaning up log files..."
    for log_file in gateway.log identity-service.log fleet-service.log trip-service.log ledger-service.log reporting-service.log; do
        if [ -f "$log_file" ]; then
            print_info "Removing log file: $log_file"
            rm -f "$log_file"
        fi
    done

    # Check for Docker database services
    if command -v docker &> /dev/null; then
        print_info "Checking for Docker database services..."
        if docker ps --format "table {{.Names}}" | grep -q "fleet"; then
            print_warning "Docker compose services are running"
            print_info "To stop databases: docker compose -f infra/docker/docker-compose.yml down"
        else
            print_info "No Docker compose services found running"
        fi
    else
        print_warning "Docker not found, skipping database service checks"
    fi

    print_info "✅ Backend stop operation completed"
}

# Function to verify services are stopped
verify_services_stopped() {
    print_info "Verifying services are stopped..."
    sleep 2

    still_running=0
    for port in 8080 8081 8082 8083 8084 8085; do
        if command -v lsof &> /dev/null && lsof -i :$port > /dev/null 2>&1; then
            still_running=1
            print_warning "Port $port is still in use"
        elif command -v ss &> /dev/null && ss -tuln | grep -q ":$port\s"; then
            still_running=1
            print_warning "Port $port is still in use"
        elif command -v netstat &> /dev/null; then
            # Windows netstat uses -an flags
            if netstat -an | grep -q ":$port\s"; then
                still_running=1
                print_warning "Port $port is still in use"
            fi
        fi
    done

    if [ $still_running -eq 1 ]; then
        print_warning "Some ports are still in use"
        print_warning "Some services may still be running"
        return 1
    else
        print_info "✅ All Yatrik backend services are stopped"
        return 0
    fi
}

# Function to display status
print_status() {
    echo ""
    echo "=== Backend Services Status ==="
    echo "The following backend services should be stopped:"
    echo "Gateway:            http://localhost:8080"
    echo "Identity Service:   http://localhost:8081"
    echo "Fleet Service:      http://localhost:8082"
    echo "Trip Service:       http://localhost:8083"
    echo "Ledger Service:     http://localhost:8084"
    echo "Reporting Service:  http://localhost:8085"
    echo ""
    echo "=== Database Services ==="
    echo "Local PostgreSQL databases (ports 5432-5435):"
    echo "  - identity-db: port 5432"
    echo "  - fleet-db:    port 5433"
    echo "  - trip-db:     port 5434"
    echo "  - ledger-db:   port 5435"
    echo ""
    echo "=== Log Files (in project root) ==="
    echo "Gateway: gateway.log"
    echo "Identity Service: identity-service.log"
    echo "Fleet Service: fleet-service.log"
    echo "Trip Service: trip-service.log"
    echo "Ledger Service: ledger-service.log"
    echo "Reporting Service: reporting-service.log"
}

# Function to display usage
print_usage() {
    cat << EOF

=== Yatrik Fleet Platform Backend Stop Script ===

USAGE:
  $0 [OPTIONS]

OPTIONS:
  --stop           Stop all Yatrik backend services (default)
  --status         Show current service status
  --help           Show this help message

EXAMPLES:
  # Stop all Yatrik backend services
  $0 --stop

  # Check service status
  $0 --status

  # Show help
  $0 --help

NOTES:
  - This script stops all Yatrik backend Java services
  - Database services (if using Docker) are not stopped
  - To stop Docker databases: docker compose -f infra/docker/docker-compose.yml down
  - Log files will be cleaned up

EOF
}

# Main script logic
main() {
    # Default action is to stop backend
    action="stop"

    # Parse command line arguments
    for arg in "$@"; do
        case $arg in
            --stop)
                action="stop"
                shift
                ;;
            --status)
                action="status"
                shift
                ;;
            --help)
                print_usage
                exit 0
                ;;
            *)
                print_error "Unknown option: $arg"
                print_usage
                exit 1
                ;;
        esac
    done

    case $action in
        "stop")
            stop_all_services
            verify_services_stopped
            print_status
            ;;

        "status")
            print_info "=== Yatrik Backend Services Status ==="
            print_status
            ;;
    esac
}

# Run main function
main "$@"