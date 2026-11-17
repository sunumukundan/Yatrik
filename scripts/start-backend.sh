#!/bin/bash
# Starts the Yatrik Identity Service backend on port 8095

set -e

PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../identity-service" && pwd)"
PROFILE="${1:-default}"
SKIP_TESTS="${2:-true}"

echo "Starting Yatrik Identity Service on port 8095..."
echo "Project: $PROJECT_DIR"
echo "Profile: $PROFILE"

cd "$PROJECT_DIR"

# Build the project first
echo "Building project..."
BUILD_ARGS=("clean" "package")
if [[ "$SKIP_TESTS" == "true" ]]; then
    BUILD_ARGS+=("-DskipTests")
fi
mvn "${BUILD_ARGS[@]}"

echo "Build successful. Starting application..."

# Start the application
export SERVER_PORT=8095
export SPRING_PROFILES_ACTIVE="$PROFILE"

mvn spring-boot:run