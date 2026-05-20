#!/bin/sh
# setup-vault.sh — Initialize HashiCorp Vault with SwasthyaSetu secrets
# Run after: docker compose -f docker-compose.yml -f docker-compose.vault.yml up -d vault
set -eu

VAULT_ADDR="${VAULT_ADDR:-http://127.0.0.1:8200}"
VAULT_TOKEN="${VAULT_DEV_ROOT_TOKEN:-swasthya-root-token}"

export VAULT_ADDR
export VAULT_TOKEN

echo "============================================="
echo " SwasthyaSetu — Vault Secret Initialization"
echo "============================================="
echo "Vault address : $VAULT_ADDR"
echo ""

# Wait for Vault to be ready
echo "Waiting for Vault to be ready..."
for i in $(seq 1 20); do
  vault status > /dev/null 2>&1 && break
  echo "  ($i/20) Vault not ready yet..."
  sleep 3
done
vault status
echo ""

# ---------------------------------------------------------
# 1. Enable KV v2 secrets engine at path swasthya-setu/
# ---------------------------------------------------------
echo "[1/4] Enabling KV-v2 secrets engine at path: swasthya-setu/"
vault secrets enable -path=swasthya-setu kv-v2 2>/dev/null || echo "  (already enabled)"

# ---------------------------------------------------------
# 2. Store database credentials
# ---------------------------------------------------------
echo "[2/4] Storing database credentials..."
vault kv put swasthya-setu/database \
  POSTGRES_USER="${POSTGRES_USER:-myuser}" \
  POSTGRES_PASSWORD="${POSTGRES_PASSWORD:-myuser}" \
  POSTGRES_DB="${POSTGRES_DB:-swasthyasetudb}"
echo "  ✅ swasthya-setu/database"

# ---------------------------------------------------------
# 3. Store Docker registry credentials
# ---------------------------------------------------------
echo "[3/4] Storing Docker registry credentials..."
vault kv put swasthya-setu/registry \
  DOCKER_REGISTRY_URL="${DOCKER_REGISTRY_URL:-docker.io}" \
  DOCKER_REGISTRY_USERNAME="${REGISTRY_USERNAME:-adityapareek01}" \
  DOCKER_REGISTRY_PASSWORD="${REGISTRY_PASSWORD:-changeme}"
echo "  ✅ swasthya-setu/registry"

# ---------------------------------------------------------
# 4. Store application secrets (JWT, RabbitMQ, etc.)
# ---------------------------------------------------------
echo "[4/4] Storing application secrets..."
vault kv put swasthya-setu/app \
  JWT_SECRET="${JWT_SECRET:-swasthya-jwt-secret-changeme-in-production}" \
  RABBITMQ_DEFAULT_USER="${RABBITMQ_DEFAULT_USER:-guest}" \
  RABBITMQ_DEFAULT_PASS="${RABBITMQ_DEFAULT_PASS:-guest}" \
  MAIL_HOST="${MAIL_HOST:-mailpit}" \
  MAIL_PORT="${MAIL_PORT:-1025}"
echo "  ✅ swasthya-setu/app"

# ---------------------------------------------------------
# Summary — verify secrets are stored
# ---------------------------------------------------------
echo ""
echo "============================================="
echo " Stored Secrets Summary"
echo "============================================="
echo "Paths:"
vault kv list swasthya-setu/
echo ""
echo "Database secret keys:"
vault kv get -format=json swasthya-setu/database | python3 -c "import sys,json; d=json.load(sys.stdin); [print('  -', k) for k in d['data']['data'].keys()]"
echo ""
echo "Registry secret keys:"
vault kv get -format=json swasthya-setu/registry | python3 -c "import sys,json; d=json.load(sys.stdin); [print('  -', k) for k in d['data']['data'].keys()]"
echo ""
echo "App secret keys:"
vault kv get -format=json swasthya-setu/app | python3 -c "import sys,json; d=json.load(sys.stdin); [print('  -', k) for k in d['data']['data'].keys()]"
echo ""
echo "✅ Vault initialization complete!"
echo "   UI available at: $VAULT_ADDR/ui"
echo "   Root Token: $VAULT_TOKEN"
