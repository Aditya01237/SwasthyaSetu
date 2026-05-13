#!/bin/sh
set -eu

IMAGE_REPOSITORY_PREFIX="${IMAGE_REPOSITORY_PREFIX:-docker.io/adityapareek01}"
IMAGE_TAG="${IMAGE_TAG:-$(git rev-parse --short HEAD 2>/dev/null || echo local)}"
APP_IMAGE_SERVICES="${APP_IMAGE_SERVICES:-ai-service backend auth-service hospital-service appointment-service patient-service notification-service api-gateway swasthya-frontend doctor-frontend}"

export IMAGE_REPOSITORY_PREFIX
export IMAGE_TAG

echo "Building application images with tag: ${IMAGE_REPOSITORY_PREFIX}/*:${IMAGE_TAG}"
export BUILDX_NO_DEFAULT_ATTESTATIONS=1
docker compose -f docker-compose.yml -f docker-compose.images.yml build ${APP_IMAGE_SERVICES}

echo "Pushing application images..."
docker compose -f docker-compose.yml -f docker-compose.images.yml push ${APP_IMAGE_SERVICES}

echo "Published application images with tag: ${IMAGE_TAG}"
