#!/usr/bin/env bash

set -euo pipefail

CONNECT_URL="${CONNECT_URL:-http://localhost:18093}"
CONNECTOR_NAME="order-outbox-source"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
CONNECTOR_FILE="${SCRIPT_DIR}/../connectors/order-outbox-source.json"

echo "Registering connector ${CONNECTOR_NAME} to ${CONNECT_URL}"

curl -sS -X POST \
  -H "Content-Type: application/json" \
  --data @"${CONNECTOR_FILE}" \
  "${CONNECT_URL}/connectors"

echo
echo "Done."
