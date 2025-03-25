#!/usr/bin/env bash

# Script to modify and revert Docker configuration files for Linux compatibility
# This script handles modifications for host.docker.internal in Linux environments

set -e  # Exit on any error

# Define paths
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
DOCKER_DIR="$(cd "${SCRIPT_DIR}/../clients/docker" && pwd)"
COMPOSE_FILE="${DOCKER_DIR}/docker-compose.observability.yml"
PROMETHEUS_FILE="${DOCKER_DIR}/prometheus.yml"

# Function to check if files exist
check_files() {
  if [ ! -f "$COMPOSE_FILE" ]; then
    echo "Error: Docker Compose file not found at $COMPOSE_FILE"
    exit 1
  fi

  if [ ! -f "$PROMETHEUS_FILE" ]; then
    echo "Error: Prometheus config file not found at $PROMETHEUS_FILE"
    exit 1
  }

  # Check if we're in a git repository
  if ! git -C "$DOCKER_DIR" rev-parse --is-inside-work-tree > /dev/null 2>&1; then
    echo "Warning: The Docker directory is not a git repository. Cannot use git to revert changes."
    GIT_AVAILABLE=false
  else
    GIT_AVAILABLE=true
  fi
}

# Function to modify files for Linux compatibility
modify_for_linux() {
  echo "Modifying files for Linux compatibility..."

  # Backup original files
  cp "$COMPOSE_FILE" "${COMPOSE_FILE}.bak"
  cp "$PROMETHEUS_FILE" "${PROMETHEUS_FILE}.bak"

  # Modify docker-compose.observability.yml - add network_mode: "host" to prometheus service
  sed -i '/prometheus:/,/grafana:/ s/^\(  \)\(container_name: prometheus\)/\1\2\n\1network_mode: "host"/' "$COMPOSE_FILE"

  # Remove ports section from prometheus service since it's not needed with host network
  sed -i '/prometheus:/,/grafana:/ {/ports:/,/- [0-9]*:[0-9]*/d}' "$COMPOSE_FILE"

  # Modify prometheus.yml - replace host.docker.internal with localhost
  sed -i 's/host\.docker\.internal/localhost/g' "$PROMETHEUS_FILE"

  echo "✅ Files modified successfully"
  echo "   - Added network_mode: \"host\" to prometheus service"
  echo "   - Removed ports section from prometheus service"
  echo "   - Replaced host.docker.internal with localhost in prometheus.yml"
  echo
  echo "To revert these changes, run: $0 revert"
}

# Function to revert changes
revert_changes() {
  echo "Reverting changes..."

  if $GIT_AVAILABLE; then
    # Use git to revert changes
    git -C "$DOCKER_DIR" checkout -- "$COMPOSE_FILE" "$PROMETHEUS_FILE"
    echo "✅ Changes reverted using git"
  elif [ -f "${COMPOSE_FILE}.bak" ] && [ -f "${PROMETHEUS_FILE}.bak" ]; then
    # Use backup files
    mv "${COMPOSE_FILE}.bak" "$COMPOSE_FILE"
    mv "${PROMETHEUS_FILE}.bak" "$PROMETHEUS_FILE"
    echo "✅ Changes reverted using backup files"
  else
    echo "❌ Cannot revert changes: No backup files found and git is not available"
    exit 1
  fi
}

# Main logic
check_files

case "$1" in
  revert)
    revert_changes
    ;;
  *)
    modify_for_linux
    ;;
esac

echo "Done."