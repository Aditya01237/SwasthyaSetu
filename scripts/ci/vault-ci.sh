#!/bin/sh
# Start HashiCorp Vault (dev mode) via Compose and seed KV paths used by SwasthyaSetu.
# Registry credentials must be in the environment (Jenkins injects from credentials before calling this).
# Requires: docker compose, docker. Optional: curl for health wait.
set -eu

ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
cd "$ROOT"

COMPOSE_FILES="-f docker-compose.yml -f docker-compose.vault.yml"
VAULT_CONTAINER="${VAULT_CONTAINER:-swasthya-setu-vault}"
VAULT_TOKEN="${VAULT_DEV_ROOT_TOKEN:-swasthya-root-token}"

echo "Starting Vault container..."
docker compose $COMPOSE_FILES up -d vault

echo "Waiting for Vault to become healthy..."
attempt=0
while [ "$attempt" -lt 60 ]; do
  status="$(docker inspect --format='{{if .State.Health}}{{.State.Health.Status}}{{else}}{{.State.Status}}{{end}}' "$VAULT_CONTAINER" 2>/dev/null || echo none)"
  if [ "$status" = "healthy" ] || [ "$status" = "running" ]; then
    if docker exec -e VAULT_TOKEN="$VAULT_TOKEN" "$VAULT_CONTAINER" vault status -address=http://127.0.0.1:8200 >/dev/null 2>&1; then
      echo "Vault is ready."
      break
    fi
  fi
  attempt=$((attempt + 1))
  sleep 2
done

if ! docker exec -e VAULT_TOKEN="$VAULT_TOKEN" "$VAULT_CONTAINER" vault status -address=http://127.0.0.1:8200 >/dev/null 2>&1; then
  echo "Vault did not become ready in time."
  exit 1
fi

vexec() {
  docker exec -e VAULT_TOKEN="$VAULT_TOKEN" -e VAULT_ADDR=http://127.0.0.1:8200 "$VAULT_CONTAINER" "$@"
}

echo "Enabling KV-v2 at swasthya-setu/ ..."
vexec vault secrets enable -path=swasthya-setu kv-v2 2>/dev/null || echo "  (engine may already exist)"

echo "Writing swasthya-setu/database ..."
vexec vault kv put swasthya-setu/database \
  POSTGRES_USER="${POSTGRES_USER:-myuser}" \
  POSTGRES_PASSWORD="${POSTGRES_PASSWORD:-myuser}" \
  POSTGRES_DB="${POSTGRES_DB:-swasthyasetudb}"

echo "Writing swasthya-setu/registry (from CI environment) ..."
if [ -z "${REGISTRY_USERNAME:-}" ] || [ -z "${REGISTRY_PASSWORD:-}" ]; then
  echo "REGISTRY_USERNAME and REGISTRY_PASSWORD must be set (e.g. from Jenkins credentials binding)."
  exit 1
fi
vexec vault kv put swasthya-setu/registry \
  DOCKER_REGISTRY_URL="${DOCKER_REGISTRY_URL:-docker.io}" \
  DOCKER_REGISTRY_USERNAME="${REGISTRY_USERNAME}" \
  DOCKER_REGISTRY_PASSWORD="${REGISTRY_PASSWORD}"

echo "Writing swasthya-setu/app ..."
vexec vault kv put swasthya-setu/app \
  JWT_SECRET="${JWT_SECRET:-swasthya-jwt-secret-changeme-in-production}" \
  RABBITMQ_DEFAULT_USER="${RABBITMQ_DEFAULT_USER:-guest}" \
  RABBITMQ_DEFAULT_PASS="${RABBITMQ_DEFAULT_PASS:-guest}" \
  MAIL_HOST="${MAIL_HOST:-mailpit}" \
  MAIL_PORT="${MAIL_PORT:-1025}"

echo "Vault KV paths:"
vexec vault kv list swasthya-setu/

echo "Vault seeding complete."
