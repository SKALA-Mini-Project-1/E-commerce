#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)
REPO_ROOT=$(cd "$SCRIPT_DIR/../.." && pwd)
LOG_DIR="$REPO_ROOT/tmp/load-test-logs"

mkdir -p "$LOG_DIR"

SERVICE=order
DURATION_SEC=180
SIZE_MB=
STEP_MB=20
STEP_INTERVAL_SEC=5
PARALLEL=
START_JITTER_SEC=2
BASE_URL=

usage() {
  cat <<'EOF'
Usage:
  load-memory.sh [options]

Options:
  -s, --service            order | payment | product
  -u, --base-url           Direct service base URL. If omitted, a local port-forward URL is used.
  -p, --parallel           Number of concurrent memory requests
  --duration-sec           Hold duration in seconds (default: 180)
  --size-mb                Target allocated size in MB
  --step-mb                Step allocation size in MB (default: 20)
  --step-interval-sec      Allocation interval in seconds (default: 5)
  --start-jitter-sec       Delay between worker launches in seconds (default: 2)
  -h, --help               Show help

Examples:
  ./scripts/keda/load-memory.sh --service order --parallel 1 --size-mb 320
  ./scripts/keda/load-memory.sh --service payment --parallel 2 --size-mb 300 --duration-sec 240
EOF
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    -s|--service) SERVICE=$2; shift 2 ;;
    -u|--base-url) BASE_URL=$2; shift 2 ;;
    -p|--parallel) PARALLEL=$2; shift 2 ;;
    --duration-sec) DURATION_SEC=$2; shift 2 ;;
    --size-mb) SIZE_MB=$2; shift 2 ;;
    --step-mb) STEP_MB=$2; shift 2 ;;
    --step-interval-sec) STEP_INTERVAL_SEC=$2; shift 2 ;;
    --start-jitter-sec) START_JITTER_SEC=$2; shift 2 ;;
    -h|--help) usage; exit 0 ;;
    *) echo "Unknown option: $1" >&2; usage; exit 1 ;;
  esac
done

if ! command -v curl >/dev/null 2>&1; then
  echo "curl is not installed." >&2
  exit 1
fi

case "$SERVICE" in
  order)
    : "${BASE_URL:=http://127.0.0.1:18083}"
    : "${PARALLEL:=1}"
    : "${SIZE_MB:=320}"
    ;;
  payment)
    : "${BASE_URL:=http://127.0.0.1:18084}"
    : "${PARALLEL:=2}"
    : "${SIZE_MB:=300}"
    ;;
  product)
    : "${BASE_URL:=http://127.0.0.1:18082}"
    : "${PARALLEL:=2}"
    : "${SIZE_MB:=260}"
    ;;
  *)
    echo "Unsupported service: $SERVICE" >&2
    exit 1
    ;;
esac

TIMESTAMP=$(date +%Y%m%d-%H%M%S)
RUN_DIR="$LOG_DIR/memory-${SERVICE}-${TIMESTAMP}"
mkdir -p "$RUN_DIR"

URL="${BASE_URL%/}/api/load-memory?duration-sec=${DURATION_SEC}&size-mb=${SIZE_MB}&step-mb=${STEP_MB}&step-interval-sec=${STEP_INTERVAL_SEC}"

cat <<EOF
Starting memory load test
- service: $SERVICE
- url: $URL
- parallel requests: $PARALLEL
- log dir: $RUN_DIR
EOF

pids=()
for worker in $(seq 1 "$PARALLEL"); do
  output_file="$RUN_DIR/worker-${worker}.json"
  (
    curl -fsS "$URL" | tee "$output_file"
  ) &
  pids+=($!)
  if [[ "$worker" -lt "$PARALLEL" && "$START_JITTER_SEC" -gt 0 ]]; then
    sleep "$START_JITTER_SEC"
  fi
done

status=0
for pid in "${pids[@]}"; do
  if ! wait "$pid"; then
    status=1
  fi
done

if [[ "$status" -ne 0 ]]; then
  echo "One or more memory load workers failed. Check $RUN_DIR" >&2
  exit "$status"
fi

echo "Memory load test completed. Results are in $RUN_DIR"
