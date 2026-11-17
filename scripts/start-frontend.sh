#!/bin/bash
# Starts the Yatrik Web frontend on port 5173

set -e

PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../web" && pwd)"
INSTALL_DEPS="${1:-false}"

echo "Starting Yatrik Web Frontend on port 5173..."
echo "Project: $PROJECT_DIR"

cd "$PROJECT_DIR"

if [[ "$INSTALL_DEPS" == "true" ]]; then
    echo "Installing dependencies..."
    npm install
fi

echo "Starting Vite dev server..."

# Start the dev server
npm run dev