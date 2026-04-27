#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)
REPO_ROOT=$(cd "$SCRIPT_DIR/../.." && pwd)
PID_DIR="$REPO_ROOT/tmp/keda-local/service-access"

if [[ ! -d "$PID_DIR" ]]; then
  echo "No local service access state found."
  exit 0
fi

for pid_file in "$PID_DIR"/*.pid; do
  [[ -e "$pid_file" ]] || continue
  pid=$(cat "$pid_file")
  if kill -0 "$pid" 2>/dev/null; then
    kill "$pid"
    echo "Stopped $(basename "$pid_file" .pid)"
  fi
  rm -f "$pid_file"
done
