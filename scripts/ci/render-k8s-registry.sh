#!/bin/sh
set -eu

IMAGE_REPOSITORY_PREFIX="${IMAGE_REPOSITORY_PREFIX:-docker.io/adityapareek01}"
IMAGE_TAG="${IMAGE_TAG:-$(git rev-parse --short HEAD 2>/dev/null || echo local)}"

require_command() {
  if ! command -v "$1" >/dev/null 2>&1; then
    echo "$1 is required to render Kubernetes registry manifests."
    exit 1
  fi
}

reject_spaces() {
  value_name="$1"
  value="$2"
  case "$value" in
    *" "*)
      echo "$value_name must not contain spaces."
      exit 1
      ;;
  esac
}

require_command kubectl
reject_spaces IMAGE_REPOSITORY_PREFIX "$IMAGE_REPOSITORY_PREFIX"
reject_spaces IMAGE_TAG "$IMAGE_TAG"

SCRIPT_DIR="$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)"
REPO_ROOT="$(CDPATH= cd -- "$SCRIPT_DIR/../.." && pwd)"
OVERLAY_ROOT="$REPO_ROOT/.tmp/k8s-registry-overlay-$$"

cleanup() {
  rm -rf "$OVERLAY_ROOT"
}
trap cleanup EXIT INT TERM

mkdir -p "$OVERLAY_ROOT"

cat >"$OVERLAY_ROOT/kustomization.yaml" <<EOF
apiVersion: kustomize.config.k8s.io/v1beta1
kind: Kustomization

resources:
  - ../../k8s

images:
  - name: swasthya-setu-ai-service
    newName: ${IMAGE_REPOSITORY_PREFIX}/swasthya-setu-ai-service
    newTag: ${IMAGE_TAG}
  - name: swasthya-setu-backend
    newName: ${IMAGE_REPOSITORY_PREFIX}/swasthya-setu-backend
    newTag: ${IMAGE_TAG}
  - name: swasthya-setu-auth-service
    newName: ${IMAGE_REPOSITORY_PREFIX}/swasthya-setu-auth-service
    newTag: ${IMAGE_TAG}
  - name: swasthya-setu-hospital-service
    newName: ${IMAGE_REPOSITORY_PREFIX}/swasthya-setu-hospital-service
    newTag: ${IMAGE_TAG}
  - name: swasthya-setu-appointment-service
    newName: ${IMAGE_REPOSITORY_PREFIX}/swasthya-setu-appointment-service
    newTag: ${IMAGE_TAG}
  - name: swasthya-setu-patient-service
    newName: ${IMAGE_REPOSITORY_PREFIX}/swasthya-setu-patient-service
    newTag: ${IMAGE_TAG}
  - name: swasthya-setu-notification-service
    newName: ${IMAGE_REPOSITORY_PREFIX}/swasthya-setu-notification-service
    newTag: ${IMAGE_TAG}
  - name: swasthya-setu-api-gateway
    newName: ${IMAGE_REPOSITORY_PREFIX}/swasthya-setu-api-gateway
    newTag: ${IMAGE_TAG}
  - name: swasthya-setu-swasthya-frontend
    newName: ${IMAGE_REPOSITORY_PREFIX}/swasthya-setu-patient-frontend
    newTag: ${IMAGE_TAG}
  - name: swasthya-setu-doctor-frontend
    newName: ${IMAGE_REPOSITORY_PREFIX}/swasthya-setu-doctor-frontend
    newTag: ${IMAGE_TAG}
EOF

kubectl kustomize "$OVERLAY_ROOT"
