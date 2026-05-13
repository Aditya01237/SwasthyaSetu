#!/bin/sh
set -eu

RETRIES="${HEALTH_RETRIES:-90}"
SLEEP_SECONDS="${HEALTH_SLEEP_SECONDS:-2}"

check_url() {
  name="$1"
  url="$2"
  attempt=1

  while [ "$attempt" -le "$RETRIES" ]; do
    if curl -fsS "$url" >/dev/null; then
      echo "$name is reachable: $url"
      return 0
    fi

    echo "Waiting for $name ($attempt/$RETRIES)..."
    attempt=$((attempt + 1))
    sleep "$SLEEP_SECONDS"
  done

  echo "$name did not become reachable: $url"
  return 1
}

check_url "Elasticsearch" "http://localhost:${ELASTICSEARCH_PORT:-9200}/_cluster/health"
check_url "Logstash API" "http://localhost:${LOGSTASH_API_PORT:-9600}/_node/pipelines"
check_url "Kibana" "http://localhost:${KIBANA_PORT:-5601}/api/status"
