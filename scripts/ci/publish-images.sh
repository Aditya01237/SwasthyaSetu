#!/bin/sh
set -eu

IMAGE_REPOSITORY_PREFIX="${IMAGE_REPOSITORY_PREFIX:-docker.io/adityapareek01}"
IMAGE_TAG="${IMAGE_TAG:-$(git rev-parse --short HEAD 2>/dev/null || echo local)}"
APP_IMAGE_SERVICES="${APP_IMAGE_SERVICES:-ai-service backend auth-service hospital-service appointment-service patient-service notification-service api-gateway swasthya-frontend doctor-frontend}"

export IMAGE_REPOSITORY_PREFIX
export IMAGE_TAG

CURRENT_COMMIT="$(git rev-parse --short=12 HEAD 2>/dev/null || echo unknown)"
echo "Publishing from git commit: ${CURRENT_COMMIT}"
echo "Resolved publish images:"
docker compose -f docker-compose.yml -f docker-compose.images.yml config \
  | sed -n '/image: /p'

if [ "${DOCKER_REGISTRY_URL:-docker.io}" = "docker.io" ]; then
  if docker compose -f docker-compose.yml -f docker-compose.images.yml config \
    | grep -E "image: docker\\.io/[^/]+/(ai-service|backend|auth-service|hospital-service|appointment-service|patient-service|notification-service|api-gateway|swasthya-frontend|doctor-frontend):" >/dev/null; then
    echo "Docker Hub image names are using the old service-only repository layout."
    echo "Expected names like docker.io/adityapareek01/swasthya-setu-auth-service:${IMAGE_TAG}."
    echo "Make sure Jenkins is checking out the latest aditya-branch commit before publishing."
    exit 1
  fi
fi

echo "Building application images with tag: ${IMAGE_REPOSITORY_PREFIX}/*:${IMAGE_TAG}"
export BUILDX_NO_DEFAULT_ATTESTATIONS=1
docker compose -f docker-compose.yml -f docker-compose.images.yml build ${APP_IMAGE_SERVICES}

echo "Pushing application images..."
for service in ${APP_IMAGE_SERVICES}; do
  echo "Pushing ${service}..."
  docker compose -f docker-compose.yml -f docker-compose.images.yml push "${service}"
done

echo "Published application images with tag: ${IMAGE_TAG}"
