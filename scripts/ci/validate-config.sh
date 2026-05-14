#!/bin/sh
set -eu

echo "Validating Docker Compose configuration..."
docker compose config --quiet
docker compose -f docker-compose.yml -f docker-compose.images.yml config --quiet
docker compose -f docker-compose.yml -f docker-compose.service-dbs.yml config --quiet
docker compose -f docker-compose.yml -f docker-compose.observability.yml config --quiet
docker compose -f docker-compose.yml -f docker-compose.service-dbs.yml -f docker-compose.observability.yml config --quiet
docker compose -f docker-compose.yml -f docker-compose.images.yml -f docker-compose.service-dbs.yml -f docker-compose.observability.yml config --quiet
docker compose -f docker-compose.yml -f docker-compose.vault.yml config --quiet

if command -v kubectl >/dev/null 2>&1; then
  echo "Rendering Kubernetes manifests with kubectl kustomize..."
  kubectl kustomize k8s >/tmp/swasthya-setu-k8s.yaml
else
  echo "kubectl not found; skipping Kubernetes manifest render."
fi

echo "Configuration validation complete."
