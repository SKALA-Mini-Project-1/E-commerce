#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)
REPO_ROOT=$(cd "$SCRIPT_DIR/../.." && pwd)
PID_DIR="$REPO_ROOT/tmp/keda-local/service-access"
LOG_DIR="$PID_DIR/logs"

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

start_proxy() {
  local pid_file="$PID_DIR/local-port-proxy.pid"
  local log_file="$LOG_DIR/local-port-proxy.log"

  if is_running "$pid_file"; then
    echo "local-port-proxy is already running on localhost:18090"
    return
  fi

  nohup python3 "$REPO_ROOT/tmp/local_port_proxy.py" >"$log_file" 2>&1 &
  echo $! >"$pid_file"
  sleep 1
  echo "Started local proxy -> http://127.0.0.1:18090"
}

require_cmd kubectl
require_cmd python3

start_port_forward "user-service" "ecommerce" "user-service" "18081" "8080"
start_port_forward "product-service" "ecommerce" "product-service" "18082" "8080"
start_port_forward "order-service" "ecommerce" "order-service" "18083" "8080"
start_port_forward "payment-service" "ecommerce" "payment-service" "18084" "8080"
start_proxy

cat <<'EOF'

Local service access is ready.

- Front proxy: http://127.0.0.1:18090
- user-service: http://127.0.0.1:18081
- product-service: http://127.0.0.1:18082
- order-service: http://127.0.0.1:18083
- payment-service: http://127.0.0.1:18084

Examples:
- hey -z 5m -c 20 http://127.0.0.1:18090/api/orders
- curl "http://127.0.0.1:18083/api/load-memory?duration-sec=180&size-mb=320&step-mb=20&step-interval-sec=5"
EOF
