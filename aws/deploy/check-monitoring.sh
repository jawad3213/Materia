#!/bin/bash
# ===================================================================
# Post-deploy monitoring gate, run on the EC2 host by SSM Run Command
# (production CD, "Monitoring" stage).
#
# Watches the new release in Prometheus for OBSERVE_SECONDS (default
# 300), then fails when:
#   - a scrape target is down (backend, host, PostgreSQL...)
#   - a "critical" alert fires (aws/deploy/monitoring/prometheus/alerts.yml)
#   - more than MAX_ERROR_RATIO (default 0.05) of the API requests
#     answered 5xx during the observation
# and prints the release's figures (traffic, errors, latency, memory).
# ===================================================================
set -euo pipefail

OBSERVE_SECONDS="${OBSERVE_SECONDS:-300}"
MAX_ERROR_RATIO="${MAX_ERROR_RATIO:-0.05}"
PROMETHEUS="${PROMETHEUS:-http://127.0.0.1:9090}"

log() { echo "[monitoring $(date -u +%H:%M:%S)] $*"; }

# query <promql> -> the first value of an instant query, or "none"
query() {
  curl -fsS --get "$PROMETHEUS/api/v1/query" --data-urlencode "query=$1" |
    python3 -c 'import json,sys; r=json.load(sys.stdin)["data"]["result"]; print(r[0]["value"][1] if r else "none")'
}

# lines <promql> <label> -> one "<label value>" per series
lines() {
  curl -fsS --get "$PROMETHEUS/api/v1/query" --data-urlencode "query=$1" |
    python3 -c 'import json,sys; [print(s["metric"].get(sys.argv[1], "?")) for s in json.load(sys.stdin)["data"]["result"]]' "$2"
}

for _ in $(seq 1 30); do
  curl -fsS "$PROMETHEUS/-/ready" >/dev/null 2>&1 && break
  sleep 5
done
curl -fsS "$PROMETHEUS/-/ready" >/dev/null || { log "Prometheus is not ready"; exit 1; }

log "Observing the release for ${OBSERVE_SECONDS}s"
sleep "$OBSERVE_SECONDS"
WINDOW="${OBSERVE_SECONDS}s"
API='job="backend", uri!~"/actuator.*"'
FAILED=0

DOWN=$(lines 'up == 0' job)
if [ -n "$DOWN" ]; then
  log "FAIL targets down: $(echo "$DOWN" | tr '\n' ' ')"
  FAILED=1
else
  log "OK   every target is up ($(query 'count(up)') targets)"
fi

CRITICAL=$(lines 'ALERTS{alertstate="firing", severity="critical"}' alertname)
if [ -n "$CRITICAL" ]; then
  log "FAIL critical alerts firing: $(echo "$CRITICAL" | tr '\n' ' ')"
  FAILED=1
else
  log "OK   no critical alert firing"
fi
WARNINGS=$(lines 'ALERTS{alertstate="firing", severity!="critical"}' alertname)
[ -z "$WARNINGS" ] || log "WARN warnings firing: $(echo "$WARNINGS" | tr '\n' ' ')"

REQUESTS=$(query "sum(increase(http_server_requests_seconds_count{$API}[$WINDOW]))")
ERRORS=$(query "sum(increase(http_server_requests_seconds_count{$API, status=~\"5..\"}[$WINDOW])) or vector(0)")
RATIO=$(python3 -c 'import sys; r,e=sys.argv[1],sys.argv[2]; print(0 if r in ("none","0") or float(r)==0 else float(e)/float(r))' "$REQUESTS" "$ERRORS")
if python3 -c 'import sys; sys.exit(0 if float(sys.argv[1]) <= float(sys.argv[2]) else 1)' "$RATIO" "$MAX_ERROR_RATIO"; then
  log "OK   5xx ratio $RATIO (limit $MAX_ERROR_RATIO)"
else
  log "FAIL 5xx ratio $RATIO above $MAX_ERROR_RATIO"
  FAILED=1
fi

echo "MONITORING requests=$REQUESTS"
echo "MONITORING errors_5xx=$ERRORS"
echo "MONITORING p95_seconds=$(query "histogram_quantile(0.95, sum by (le) (rate(http_server_requests_seconds_bucket{$API}[$WINDOW])))")"
echo "MONITORING heap_used_ratio=$(query 'sum(jvm_memory_used_bytes{job="backend", area="heap"}) / sum(jvm_memory_max_bytes{job="backend", area="heap"} > 0)')"
echo "MONITORING host_memory_available_ratio=$(query 'node_memory_MemAvailable_bytes / node_memory_MemTotal_bytes')"

if [ "$FAILED" -ne 0 ]; then
  log "The release is unhealthy"
  exit 1
fi
log "The release is healthy"
