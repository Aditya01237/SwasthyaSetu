#!/bin/sh
# Read Docker registry username/password from Vault KV and run docker login (password not printed).
# Expects Vault container already running and seeded (see vault-ci.sh).
set -eu

VAULT_CONTAINER="${VAULT_CONTAINER:-swasthya-setu-vault}"
VAULT_TOKEN="${VAULT_DEV_ROOT_TOKEN:-swasthya-root-token}"
DOCKER_REGISTRY_URL="${DOCKER_REGISTRY_URL:-docker.io}"

user="$(docker exec -e VAULT_TOKEN="$VAULT_TOKEN" -e VAULT_ADDR=http://127.0.0.1:8200 "$VAULT_CONTAINER" \
  vault kv get -mount=swasthya-setu -field=DOCKER_REGISTRY_USERNAME registry)"
pass="$(docker exec -e VAULT_TOKEN="$VAULT_TOKEN" -e VAULT_ADDR=http://127.0.0.1:8200 "$VAULT_CONTAINER" \
  vault kv get -mount=swasthya-setu -field=DOCKER_REGISTRY_PASSWORD registry)"

if [ -z "$user" ] || [ -z "$pass" ]; then
  echo "Vault registry secret is empty; run vault-ci.sh with Jenkins credentials first."
  exit 1
fi

printf '%s' "$pass" | docker login "$DOCKER_REGISTRY_URL" -u "$user" --password-stdin
echo "Docker login succeeded using credentials sourced from Vault."
