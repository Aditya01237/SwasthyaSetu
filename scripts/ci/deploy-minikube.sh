#!/bin/sh
set -eu

MINIKUBE_PROFILE="${MINIKUBE_PROFILE:-minikube}"
K8S_NAMESPACE="${K8S_NAMESPACE:-swasthya-setu}"
MINIKUBE_START="${MINIKUBE_START:-true}"
K8S_BUILD_IMAGES="${K8S_BUILD_IMAGES:-true}"
K8S_ENABLE_INGRESS="${K8S_ENABLE_INGRESS:-true}"

require_command() {
  if ! command -v "$1" >/dev/null 2>&1; then
    echo "$1 is required for Minikube deployment."
    exit 1
  fi
}

require_command docker
require_command minikube
require_command kubectl

if [ "$MINIKUBE_START" = "true" ]; then
  if minikube -p "$MINIKUBE_PROFILE" status >/dev/null 2>&1; then
    echo "Minikube profile '$MINIKUBE_PROFILE' is already running."
  else
    echo "Starting Minikube profile '$MINIKUBE_PROFILE'..."
    minikube start -p "$MINIKUBE_PROFILE"
  fi
fi

if [ "$K8S_ENABLE_INGRESS" = "true" ]; then
  echo "Ensuring Minikube ingress addon is enabled..."
  minikube -p "$MINIKUBE_PROFILE" addons enable ingress
fi

if [ "$K8S_BUILD_IMAGES" = "true" ]; then
  echo "Building Docker images inside Minikube's Docker daemon..."
  eval "$(minikube -p "$MINIKUBE_PROFILE" docker-env)"
  COMPOSE_PROJECT_NAME=swasthya-setu docker compose build
fi

echo "Applying Kubernetes manifests..."
kubectl apply -k k8s

echo "Checking Kubernetes deployment health..."
K8S_NAMESPACE="$K8S_NAMESPACE" sh scripts/ci/health-check-k8s.sh

echo "Minikube deployment complete."
