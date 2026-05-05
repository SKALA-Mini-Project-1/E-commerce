#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)
REPO_ROOT=$(cd "$SCRIPT_DIR/../.." && pwd)
PID_DIR="$REPO_ROOT/tmp/keda-local/observability"
LOG_DIR="$PID_DIR/logs"

GRAFANA_NAMESPACE=${GRAFANA_NAMESPACE:-monitoring}
PROMETHEUS_NAMESPACE=${PROMETHEUS_NAMESPACE:-monitoring}
GRAFANA_SERVICE=${GRAFANA_SERVICE:-kube-prometheus-stack-grafana}
PROMETHEUS_SERVICE=${PROMETHEUS_SERVICE:-kube-prometheus-stack-prometheus}
GRAFANA_LOCAL_PORT=${GRAFANA_LOCAL_PORT:-13000}
PROMETHEUS_LOCAL_PORT=${PROMETHEUS_LOCAL_PORT:-19090}

mkdir -p "$PID_DIR" "$LOG_DIR"

require_cmd() {
  if ! command -v "$1" >/dev/null 2>&1; then
    echo "Required command not found: $1" >&2
    exit 1
  fi
}

is_running() {
  local pid_file=$1
  [[ -f "$pid_file" ]] && kill -0 "$(cat "$pid_file")" 2>/dev/null
}

start_port_forward() {
  local name=$1
  local namespace=$2
  local service=$3
  local local_port=$4
  local remote_port=$5
  local pid_file="$PID_DIR/${name}.pid"
  local log_file="$LOG_DIR/${name}.log"

  if is_running "$pid_file"; then
    echo "$name is already running on localhost:$local_port"
    return
  fi

  nohup kubectl port-forward -n "$namespace" "svc/$service" "${local_port}:${remote_port}" \
    >"$log_file" 2>&1 &
  echo $! >"$pid_file"
  sleep 1
  echo "Started $name -> http://127.0.0.1:$local_port"
}

require_cmd kubectl

start_port_forward "grafana" "$GRAFANA_NAMESPACE" "$GRAFANA_SERVICE" "$GRAFANA_LOCAL_PORT" "80"
start_port_forward "prometheus" "$PROMETHEUS_NAMESPACE" "$PROMETHEUS_SERVICE" "$PROMETHEUS_LOCAL_PORT" "9090"

cat <<EOF

Local observability access is ready.

- Grafana: http://127.0.0.1:${GRAFANA_LOCAL_PORT}
- Prometheus: http://127.0.0.1:${PROMETHEUS_LOCAL_PORT}

Suggested dashboard file:
- ${REPO_ROOT}/k8s/monitoring/grafana-dashboard-keda-scaling.json
EOF
