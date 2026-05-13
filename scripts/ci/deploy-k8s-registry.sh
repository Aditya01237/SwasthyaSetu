#!/bin/sh
set -eu

K8S_NAMESPACE="${K8S_NAMESPACE:-swasthya-setu}"
K8S_RUN_HEALTH_CHECK="${K8S_RUN_HEALTH_CHECK:-true}"
K8S_RENDERED_MANIFEST="${K8S_RENDERED_MANIFEST:-}"

require_command() {
  if ! command -v "$1" >/dev/null 2>&1; then
    echo "$1 is required for Kubernetes registry deployment."
    exit 1
  fi
}

require_command kubectl

echo "Applying Kubernetes manifests with registry images..."
if [ -n "$K8S_RENDERED_MANIFEST" ]; then
  sh scripts/ci/render-k8s-registry.sh >"$K8S_RENDERED_MANIFEST"
  kubectl apply -f "$K8S_RENDERED_MANIFEST"
else
  sh scripts/ci/render-k8s-registry.sh | kubectl apply -f -
fi

if [ "$K8S_RUN_HEALTH_CHECK" = "true" ]; then
  echo "Checking Kubernetes deployment health..."
  K8S_NAMESPACE="$K8S_NAMESPACE" sh scripts/ci/health-check-k8s.sh
fi

echo "Kubernetes registry deployment complete."
