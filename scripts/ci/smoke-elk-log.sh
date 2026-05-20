#!/bin/sh
set -eu

ELASTICSEARCH_URL="${ELASTICSEARCH_URL:-http://localhost:${ELASTICSEARCH_PORT:-9200}}"
LOGSTASH_GELF_HOST="${LOGSTASH_GELF_HOST:-127.0.0.1}"
LOGSTASH_GELF_PORT="${LOGSTASH_GELF_PORT:-${LOGSTASH_GELF_PORT_VALUE:-12201}}"
SMOKE_ID="${SMOKE_ID:-swasthya-elk-smoke-$(date +%s)}"
RETRIES="${HEALTH_RETRIES:-45}"
SLEEP_SECONDS="${HEALTH_SLEEP_SECONDS:-2}"

require_command() {
  if ! command -v "$1" >/dev/null 2>&1; then
    echo "$1 is required for ELK smoke testing."
    exit 1
  fi
}

require_command curl
require_command python3

echo "Sending GELF smoke log to ${LOGSTASH_GELF_HOST}:${LOGSTASH_GELF_PORT}"
SMOKE_ID="$SMOKE_ID" LOGSTASH_GELF_HOST="$LOGSTASH_GELF_HOST" LOGSTASH_GELF_PORT="$LOGSTASH_GELF_PORT" python3 - <<'PY'
import json
import os
import socket
import time

smoke_id = os.environ["SMOKE_ID"]
message = {
    "version": "1.1",
    "host": "swasthya-ci",
    "short_message": f"SwasthyaSetu ELK smoke log {smoke_id}",
    "full_message": f"Centralized logging smoke event for final project evidence: {smoke_id}",
    "timestamp": time.time(),
    "level": 6,
    "_service_name": "elk-smoke",
    "_environment": "local",
    "_smoke_id": smoke_id,
}

payload = json.dumps(message).encode("utf-8")
sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
sock.sendto(payload, (os.environ["LOGSTASH_GELF_HOST"], int(os.environ["LOGSTASH_GELF_PORT"])))
sock.close()
PY

query_elasticsearch() {
  curl -fsS \
    -H 'Content-Type: application/json' \
    -X POST "${ELASTICSEARCH_URL}/swasthya-setu-logs-*/_search" \
    -d "{\"size\":1,\"query\":{\"multi_match\":{\"query\":\"${SMOKE_ID}\",\"fields\":[\"short_message\",\"message\",\"full_message\",\"smoke_id\",\"_smoke_id\"]}}}" \
    2>/dev/null
}

attempt=1
while [ "$attempt" -le "$RETRIES" ]; do
  response="$(query_elasticsearch || true)"
  hits="$(printf '%s' "$response" | python3 -c 'import json,sys; d=sys.stdin.read().strip(); print(0 if not d else (lambda t: t["value"] if isinstance(t, dict) else t)(json.loads(d)["hits"]["total"]))' 2>/dev/null || echo 0)"

  if [ "$hits" -gt 0 ]; then
    echo "ELK smoke log found in Elasticsearch: ${SMOKE_ID}"
    exit 0
  fi

  echo "Waiting for ELK smoke log (${attempt}/${RETRIES})..."
  attempt=$((attempt + 1))
  sleep "$SLEEP_SECONDS"
done

echo "ELK smoke log was not found in Elasticsearch: ${SMOKE_ID}"
exit 1
