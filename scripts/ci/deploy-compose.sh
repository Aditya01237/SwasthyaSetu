#!/bin/sh
set -eu

if [ "${RUN_SERVICE_DB_SYNC:-false}" = "true" ]; then
  echo "Deploying with service-owned database overlay..."
  docker compose -f docker-compose.yml -f docker-compose.service-dbs.yml up --build -d
  docker compose -f docker-compose.yml -f docker-compose.service-dbs.yml --profile service-dbs run --rm service-db-sync
else
  echo "Deploying with transition shared database..."
  docker compose up --build -d
fi

sh scripts/ci/health-check.sh
