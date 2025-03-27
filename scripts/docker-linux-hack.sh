#!/usr/bin/env bash

# Script to modify and revert Docker configuration files for Linux compatibility
# This script handles modifications for host.docker.internal in Linux environments

set -e  # Exit on any error

# Define paths
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
DOCKER_DIR="$(cd "${SCRIPT_DIR}/../clients/docker" && pwd)"
COMPOSE_FILE="${DOCKER_DIR}/docker-compose.observability.yml"
PROMETHEUS_FILE="${DOCKER_DIR}/prometheus.yml"
GRAFANA_DS_FILE="${DOCKER_DIR}/grafana/provisioning/datasources/prometheus.yml"

# Function to check if files exist
check_files() {
  if [ ! -f "$COMPOSE_FILE" ]; then
    echo "Error: Docker Compose file not found at $COMPOSE_FILE"
    exit 1
  fi

  if [ ! -f "$PROMETHEUS_FILE" ]; then
    echo "Error: Prometheus config file not found at $PROMETHEUS_FILE"
    exit 1
  fi

  if [ ! -f "$GRAFANA_DS_FILE" ]; then
    echo "Error: Grafana datasource config file not found at $GRAFANA_DS_FILE"
    exit 1
  fi

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
  cp "$GRAFANA_DS_FILE" "${GRAFANA_DS_FILE}.bak"

  # Modify docker-compose.observability.yml - add network_mode: "host" to prometheus service
  # This improved version uses awk instead of sed for better block processing
  awk '
  /prometheus:/ {
    print $0;
    in_prometheus = 1;
    next;
  }
  /grafana:/ {
    in_prometheus = 0;
    print $0;
    next;
  }
  in_prometheus && /container_name: prometheus/ {
    print $0;
    print "    network_mode: \"host\"";
    next;
  }
  # Skip ports section in prometheus service
  in_prometheus && /ports:/ {
    in_ports = 1;
    next;
  }
  in_prometheus && in_ports && /- [0-9]+:[0-9]+/ {
    in_ports = 0;
    next;
  }
  # Print all other lines
  {
    print $0;
  }
  ' "$COMPOSE_FILE" > "${COMPOSE_FILE}.tmp" && mv "${COMPOSE_FILE}.tmp" "$COMPOSE_FILE"

  # Modify prometheus.yml - replace host.docker.internal with localhost
  sed -i 's/host\.docker\.internal/localhost/g' "$PROMETHEUS_FILE"

  # Modify Grafana datasource config - change prometheus:9090 to localhost:9090
  sed -i 's|url: http://prometheus:9090|url: http://localhost:9090|g' "$GRAFANA_DS_FILE"

  echo "✅ Files modified successfully"
  echo "   - Added network_mode: \"host\" to prometheus service"
  echo "   - Removed ports section from prometheus service"
  echo "   - Replaced host.docker.internal with localhost in prometheus.yml"
  echo "   - Updated Grafana datasource URL to use localhost:9090"
  echo
  echo "To revert these changes, run: $0 revert"
}

# Function to revert changes
revert_changes() {
  echo "Reverting changes..."

  if $GIT_AVAILABLE; then
    # Use git to revert changes
    git -C "$DOCKER_DIR" checkout -- "$COMPOSE_FILE" "$PROMETHEUS_FILE" "$GRAFANA_DS_FILE"

    # Remove backup files
    if [ -f "${COMPOSE_FILE}.bak" ]; then
      rm "${COMPOSE_FILE}.bak"
    fi

    if [ -f "${PROMETHEUS_FILE}.bak" ]; then
      rm "${PROMETHEUS_FILE}.bak"
    fi

    if [ -f "${GRAFANA_DS_FILE}.bak" ]; then
      rm "${GRAFANA_DS_FILE}.bak"
    fi

    echo "✅ Changes reverted using git and backup files removed"
  elif [ -f "${COMPOSE_FILE}.bak" ] && [ -f "${PROMETHEUS_FILE}.bak" ] && [ -f "${GRAFANA_DS_FILE}.bak" ]; then
    # Use backup files
    mv "${COMPOSE_FILE}.bak" "$COMPOSE_FILE"
    mv "${PROMETHEUS_FILE}.bak" "$PROMETHEUS_FILE"
    mv "${GRAFANA_DS_FILE}.bak" "$GRAFANA_DS_FILE"
    echo "✅ Changes reverted using backup files"
  else
    echo "❌ Cannot revert changes: Not all backup files found and git is not available"
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