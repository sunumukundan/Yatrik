#!/bin/bash

# Yatrik Fleet Platform Frontend Stop Script
# Stops all frontend processes and cleanups
# Works on Linux/macOS and Windows (Git Bash/WSL)

# Get the script directory and project root
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$SCRIPT_DIR/.."
WEB_DIR="$PROJECT_ROOT/web"

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

# Function to stop all Yatrik frontend processes
stop_all_frontend_processes() {
    print_info "=== Stopping Yatrik Frontend Processes ==="

    # Get all npm dev processes (Vite development server)
    print_info "Finding frontend development processes..."

    # Method 1: Find npm run dev processes
    frontend_pids=$(ps aux | grep "npm run dev" | grep -v grep | awk '{print $2}')

    if [ -n "$frontend_pids" ]; then
        print_info "Found frontend processes to stop: $frontend_pids"
        for pid in $frontend_pids; do
            print_info "Stopping frontend process $pid (Vite dev server)..."
            kill $pid 2>/dev/null || true
            sleep 1
            kill -9 $pid 2>/dev/null || true
        done
        print_info "✅ All npm run dev processes stopped"
    else
        print_info "No frontend npm processes found running"
    fi

    # Method 2: Find node processes running Vite
    print_info "Checking for node/Vite processes..."
    node_pids=$(ps aux | grep "node.*vite" | grep -v grep | awk '{print $2}')
    if [ -n "$node_pids" ]; then
        print_info "Stopping node/Vite processes: $node_pids"
        for pid in $node_pids; do
            print_info "Stopping node/Vite process $pid..."
            kill $pid 2>/dev/null || true
            sleep 1
            kill -9 $pid 2>/dev/null || true
        done
    fi

    # Method 3: Check for processes on frontend ports (Vite defaults 5173+)
    print_info "Checking for processes on frontend ports..."
    if command -v lsof &> /dev/null; then
        # Get processes on common frontend ports (Vite default 5173 + CRA default 3000)
        for port in 5173 5174 5175 5176 5177 5178 3000 3001 3002 3003 3004 3005; do
            frontend_port_pids=$(lsof -ti :$port)
            if [ -n "$frontend_port_pids" ]; then
                print_info "Stopping processes on port $port: $frontend_port_pids"
                for pid in $frontend_port_pids; do
                    print_info "Stopping process $pid on frontend port $port..."
                    kill $pid 2>/dev/null || true
                    sleep 1
                    kill -9 $pid 2>/dev/null || true
                done
            fi
        done
    # Method 4: Use PowerShell on Windows (Git Bash/WSL)
    elif command -v powershell.exe &> /dev/null; then
        print_info "Stopping frontend processes using PowerShell..."
        powershell.exe -Command "
            Get-Process node -ErrorAction SilentlyContinue | Where-Object {
                \$_.CommandLine -like '*npm run dev*' -or \$_.CommandLine -like '*vite*'
            } | ForEach-Object {
                Stop-Process -Id \$_.Id -Force -ErrorAction SilentlyContinue
                Write-Host \"Stopped frontend process \$(\$_.Id)\"
            }
        " 2>/dev/null
    fi

    print_info "✅ Frontend stop operation completed"
}

# Function to verify frontend processes are stopped
verify_frontend_processes_stopped() {
    print_info "Verifying frontend processes are stopped..."
    sleep 2

    still_running=0
    if command -v lsof &> /dev/null; then
        for port in 5173 5174 5175 5176 5177 5178 3000 3001 3002 3003 3004 3005; do
            if lsof -i :$port > /dev/null 2>&1; then
                still_running=$((still_running + 1))
            fi
        done
    elif command -v netstat &> /dev/null; then
        for port in 5173 5174 5175 5176 5177 5178 3000 3001 3002 3003 3004 3005; do
            if netstat -an | grep -q ":$port\s"; then
                still_running=$((still_running + 1))
            fi
        done
    fi

    if [ $still_running -gt 0 ]; then
        print_warning "$still_running frontend port(s) are still in use"
        print_warning "Some frontend processes may still be running"
        return 1
    else
        print_info "✅ All Yatrik frontend processes are stopped"
        return 0
    fi
}

# Function to clean up temporary files
cleanup_frontend_files() {
    print_info "Cleaning up temporary frontend files..."

    # Check for temporary log files
    cd "$PROJECT_ROOT"
    if [ -f "vite.log" ]; then
        print_info "Removing vite.log file..."
        rm -f vite.log
    fi

    if [ -f "frontend-build.log" ]; then
        print_info "Removing frontend-build.log file..."
        rm -f frontend-build.log
    fi

    # Clean up node_modules if needed (optional)
    if [ "$1" = "--clean-modules" ]; then
        print_warning "Cleaning node_modules..."
        cd "$WEB_DIR"
        if [ -d "node_modules" ]; then
            print_info "Removing node_modules..."
            rm -rf node_modules
        fi
        if [ -f "package-lock.json" ]; then
            print_info "Removing package-lock.json..."
            rm -f package-lock.json
        fi
        cd "$PROJECT_ROOT"
    fi

    print_info "✅ Frontend cleanup completed"
}

# Function to display status
print_status() {
    echo ""
    echo "=== Frontend Services Status ==="
    echo "The following frontend services should be stopped:"
    echo "Vite Development Server: http://localhost:5173 (default)"
    echo ""
    echo "=== Log Files ==="
    echo "Frontend logs: Browser console and terminal output"
    echo "Environment: web/.env"
    echo ""
    echo "To view any remaining frontend processes: ps aux | grep -E '(npm|vite)'"
}

# Function to display usage
print_usage() {
    cat << EOF

=== Yatrik Fleet Platform Frontend Stop Script ===

USAGE:
  $0 [OPTIONS]

OPTIONS:
  --stop           Stop all Yatrik frontend processes (default)
  --status         Show current frontend service status
  --clean-modules  Also remove node_modules and package-lock.json
  --help           Show this help message

EXAMPLES:
  # Stop all Yatrik frontend processes
  $0 --stop

  # Stop frontend and clean up node_modules
  $0 --stop --clean-modules

  # Check frontend status
  $0 --status

  # Show help
  $0 --help

NOTES:
  - This script stops all Yatrik frontend development processes
  - Vite development servers on ports 5173+ and 3000+ are targeted
  - Log files will be cleaned up
  - Node.js processes running Vite will be stopped

EOF
}

# Main script logic
main() {
    # Default action is to stop frontend
    action="stop"
    clean_modules=false

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
            --clean-modules)
                clean_modules=true
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
            stop_all_frontend_processes
            verify_frontend_processes_stopped
            if [ "$clean_modules" = true ]; then
                cleanup_frontend_files --clean-modules
            else
                cleanup_frontend_files
            fi
            print_status
            ;;

        "status")
            print_info "=== Yatrik Frontend Services Status ==="
            print_status
            ;;
    esac
}

# Run main function
main "$@"