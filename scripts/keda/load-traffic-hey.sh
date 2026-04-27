#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)
REPO_ROOT=$(cd "$SCRIPT_DIR/../.." && pwd)
LOG_DIR="$REPO_ROOT/tmp/load-test-logs"

mkdir -p "$LOG_DIR"

SERVICE=order
BASE_URL=${BASE_URL:-http://127.0.0.1:18090}
DURATION=5m
CONCURRENCY=20
QPS=15
REQUESTS=
METHOD=GET
BODY_FILE=
HEADERS=()

usage() {
  cat <<'EOF'
Usage:
  load-traffic-hey.sh [options]

Options:
  -s, --service        order | payment | product
  -u, --base-url       Base URL for the local gateway (default: http://127.0.0.1:18090)
  -z, --duration       hey duration, example: 5m, 90s
  -c, --concurrency    Concurrent workers (default: 20)
  -q, --qps            Requests per second per worker, 0 means unlimited (default: 15)
  -n, --requests       Total request count, overrides --duration when set
  -m, --method         HTTP method (default: GET)
  -d, --body-file      File passed to hey with -D
  -H, --header         Extra request header, repeatable
  -h, --help           Show help

Examples:
  ./scripts/keda/load-traffic-hey.sh --service order --duration 5m --concurrency 30 --qps 20
  ./scripts/keda/load-traffic-hey.sh --service product --requests 5000 --concurrency 40
EOF
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    -s|--service) SERVICE=$2; shift 2 ;;
    -u|--base-url) BASE_URL=$2; shift 2 ;;
    -z|--duration) DURATION=$2; shift 2 ;;
    -c|--concurrency) CONCURRENCY=$2; shift 2 ;;
    -q|--qps) QPS=$2; shift 2 ;;
    -n|--requests) REQUESTS=$2; shift 2 ;;
    -m|--method) METHOD=$2; shift 2 ;;
    -d|--body-file) BODY_FILE=$2; shift 2 ;;
    -H|--header) HEADERS+=("$2"); shift 2 ;;
    -h|--help) usage; exit 0 ;;
    *) echo "Unknown option: $1" >&2; usage; exit 1 ;;
  esac
done

if ! command -v hey >/dev/null 2>&1; then
  echo "hey is not installed." >&2
  exit 1
fi

case "$SERVICE" in
  order) PATH_SUFFIX=/api/orders ;;
  payment) PATH_SUFFIX=/api/payments ;;
  product) PATH_SUFFIX=/api/products ;;
  *)
    echo "Unsupported service: $SERVICE" >&2
    exit 1
    ;;
esac

URL="${BASE_URL%/}${PATH_SUFFIX}"
TIMESTAMP=$(date +%Y%m%d-%H%M%S)
LOG_FILE="$LOG_DIR/traffic-${SERVICE}-${TIMESTAMP}.log"

HEY_ARGS=(-disable-compression -m "$METHOD" -c "$CONCURRENCY")

if [[ -n "$REQUESTS" ]]; then
  HEY_ARGS+=(-n "$REQUESTS")
else
  HEY_ARGS+=(-z "$DURATION")
fi

if [[ "$QPS" != "0" ]]; then
  HEY_ARGS+=(-q "$QPS")
fi

if [[ -n "$BODY_FILE" ]]; then
  HEY_ARGS+=(-D "$BODY_FILE")
fi

if ((${#HEADERS[@]} > 0)); then
  for header in "${HEADERS[@]}"; do
    HEY_ARGS+=(-H "$header")
  done
fi

cat <<EOF
Starting traffic load test
- service: $SERVICE
- url: $URL
- concurrency: $CONCURRENCY
- qps per worker: $QPS
- duration: ${REQUESTS:-$DURATION}
- log: $LOG_FILE
EOF

hey "${HEY_ARGS[@]}" "$URL" | tee "$LOG_FILE"
