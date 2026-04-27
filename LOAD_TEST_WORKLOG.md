# KEDA Load Test Worklog

## Overview

- Repository: `E-commerce`
- Purpose: Validate KEDA behavior for traffic-based and memory-based scaling/rolling behavior, and observe the changes from Grafana.
- Start date: `2026-04-27`

## Goal

- Prepare repeatable load-test scripts for:
  - traffic-based load with `hey`
  - memory-based load using each service's `/api/load-memory` endpoint
- Expose Grafana locally for observation during the test
- Record commands, observations, and follow-up actions in one place

## Current Environment Notes

- `hey`: available locally
- `kubectl`: available locally
- `docker`: available locally
- `jmeter`: not installed locally at the moment
- Live cluster access from the current sandbox session was not available without external network approval

## Target Services

- `order-service`
- `payment-service`
- `product-service`

## KEDA Notes Found In Repo

- Each service has a `ScaledObject` with:
  - `memory` trigger
  - `prometheus` trigger
- Current traffic trigger threshold in manifests:
  - `threshold: "600"`
- Current memory trigger values in manifests:
  - `order-service`: `409Mi`
  - `payment-service`: `409Mi`
  - `product-service`: `307Mi`

## Planned Deliverables

- `hey` traffic load script
- memory load script
- local Grafana/Prometheus port-forward script
- Grafana dashboard JSON for observing request rate, memory, replicas, and rollout behavior
- monitoring manifest updates if needed

## Execution Log

### 2026-04-27

#### Completed

- Inspected repo structure for KEDA, monitoring, ingress, and service endpoints
- Confirmed `hey`, `kubectl`, and `docker` are installed locally
- Confirmed `jmeter` is not installed locally
- Confirmed services expose `/api/load-memory`
- Added local access, load-test, and observability helper scripts
- Added Grafana dashboard JSON for KEDA scaling observation
- Added `payment-service` and `product-service` `ServiceMonitor` manifests
- Corrected `payment-service` and `product-service` traffic trigger queries to use their own metrics
- Validated shell script syntax and dashboard JSON syntax locally
- Fixed `load-traffic-hey.sh` so it works under `set -u` when no extra `-H/--header` options are passed
- Fixed Grafana memory panel unit so `container_memory_working_set_bytes` is displayed as bytes-based memory instead of an incorrect TB-scale value

#### Findings

- `order-service` currently has an active `ServiceMonitor`
- `payment-service` has a disabled `ServiceMonitor` file
- `product-service` does not yet have an active `ServiceMonitor` manifest in `k8s/monitoring`
- `payment-service` and `product-service` traffic triggers currently point to `order-service` metrics, so traffic-based scaling is not fully aligned per service yet
- `/api/load-memory` is not exposed through the shared public API ingress, so memory tests need direct service access such as `kubectl port-forward`

#### Pending

- Verify commands against the live cluster when network access is available

## Suggested Execution Order

```bash
# 1. local service access for traffic test and memory test
./scripts/keda/start-local-service-access.sh

# 2. local Grafana / Prometheus access
./scripts/keda/start-local-observability.sh

# 3. traffic-based load example
./scripts/keda/load-traffic-hey.sh --service order --duration 5m --concurrency 30 --qps 20

# 4. memory-based load example
./scripts/keda/load-memory.sh --service order --parallel 1 --size-mb 320 --duration-sec 180

# 5. when finished
./scripts/keda/stop-local-observability.sh
./scripts/keda/stop-local-service-access.sh
```

## Grafana Dashboard Import Note

- Import file: `k8s/monitoring/grafana-dashboard-keda-scaling.json`
- Recommended local Grafana URL after port-forward: `http://127.0.0.1:13000`

#### Prepared Files

- `scripts/keda/start-local-service-access.sh`
- `scripts/keda/stop-local-service-access.sh`
- `scripts/keda/start-local-observability.sh`
- `scripts/keda/stop-local-observability.sh`
- `scripts/keda/load-traffic-hey.sh`
- `scripts/keda/load-memory.sh`
- `k8s/monitoring/servicemonitor-payment.yaml`
- `k8s/monitoring/servicemonitor-product.yaml`
- `k8s/monitoring/grafana-dashboard-keda-scaling.json`

## Test Run Template

### Run Info

- Date:
- Operator:
- Target service:
- Test type: `traffic` / `memory`
- Base URL:
- Grafana URL:
- Prometheus URL:

### Commands Used

```bash
# Fill in actual commands here
```

### Expected Behavior

- KEDA detects traffic or memory increase
- Replica count increases within the configured polling/cooldown behavior
- Grafana shows request rate, memory usage, and replica transitions

### Actual Observation

- Start replicas:
- Peak replicas:
- Scale-out start time:
- Scale-in completion time:
- Notable Grafana panels:

### Issues / Follow-up

- 

## Change Log

- `2026-04-27`: created initial worklog template before script and dashboard work
