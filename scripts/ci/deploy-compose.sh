#!/bin/sh
set -eu

# Parallel stack (Jenkins COMPOSE_PROJECT_NAME=swasthya-setu-ci): load high host ports so Redis/Rabbit/etc.
# do not collide with an already-running default `docker compose` stack on 6379, 5672, 8080, …
if [ "${COMPOSE_PROJECT_NAME:-}" = "swasthya-setu-ci" ] && [ -f .env.ci ]; then
  echo "Loading .env.ci for CI / parallel-stack host ports."
  set -a
  # shellcheck disable=SC1091
  . ./.env.ci
  set +a
fi

if [ "${RUN_SERVICE_DB_SYNC:-false}" = "true" ]; then
  echo "Deploying with service-owned database overlay..."
  docker compose -f docker-compose.yml -f docker-compose.service-dbs.yml up --build -d --remove-orphans
  docker compose -f docker-compose.yml -f docker-compose.service-dbs.yml --profile service-dbs run --rm service-db-sync
else
  echo "Deploying with transition shared database..."
  docker compose up --build -d --remove-orphans
fi

sh scripts/ci/health-check.sh
