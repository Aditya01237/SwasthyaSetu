#!/bin/sh
set -eu

COMPOSE_FILES="-f docker-compose.yml -f docker-compose.observability.yml"
ELK_START="${ELK_START:-true}"

if [ "$ELK_START" = "true" ]; then
  echo "Starting ELK services..."
  docker compose ${COMPOSE_FILES} up -d elasticsearch logstash kibana
fi

sh scripts/ci/health-check-observability.sh
sh scripts/ci/smoke-elk-log.sh

echo "ELK observability check passed."
