#!/usr/bin/env bash

set -euo pipefail

CONNECT_URL="${CONNECT_URL:-http://localhost:18093}"

echo "== connectors =="
curl -sS "${CONNECT_URL}/connectors"
echo
echo
echo "== order-outbox-source status =="
curl -sS "${CONNECT_URL}/connectors/order-outbox-source/status"
echo
