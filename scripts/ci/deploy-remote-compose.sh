#!/bin/sh
set -eu

: "${REMOTE_DEPLOY_HOST:?REMOTE_DEPLOY_HOST is required}"
: "${REMOTE_DEPLOY_USER:?REMOTE_DEPLOY_USER is required}"
: "${IMAGE_REPOSITORY_PREFIX:?IMAGE_REPOSITORY_PREFIX is required}"
: "${IMAGE_TAG:?IMAGE_TAG is required}"

REMOTE_DEPLOY_PATH="${REMOTE_DEPLOY_PATH:-swasthya-setu}"
RUN_SERVICE_DB_SYNC="${RUN_SERVICE_DB_SYNC:-false}"
SSH_OPTIONS="${SSH_OPTIONS:--o StrictHostKeyChecking=accept-new}"
SSH_TARGET="${REMOTE_DEPLOY_USER}@${REMOTE_DEPLOY_HOST}"

case "${REMOTE_DEPLOY_PATH} ${IMAGE_REPOSITORY_PREFIX} ${IMAGE_TAG}" in
  *" "*)
    echo "REMOTE_DEPLOY_PATH, IMAGE_REPOSITORY_PREFIX, and IMAGE_TAG must not contain spaces."
    exit 1
    ;;
esac

COMPOSE_FILES="-f docker-compose.yml -f docker-compose.images.yml"
if [ "${RUN_SERVICE_DB_SYNC}" = "true" ]; then
  COMPOSE_FILES="${COMPOSE_FILES} -f docker-compose.service-dbs.yml"
fi

echo "Preparing remote deployment directory: ${SSH_TARGET}:${REMOTE_DEPLOY_PATH}"
ssh ${SSH_OPTIONS} "${SSH_TARGET}" "mkdir -p ${REMOTE_DEPLOY_PATH}"

echo "Uploading Compose deployment files..."
tar -czf - \
  docker-compose.yml \
  docker-compose.images.yml \
  docker-compose.service-dbs.yml \
  docker/postgres/init-multiple-databases.sh \
  docker/postgres/sync-service-databases.sh \
  scripts/ci/health-check.sh \
  | ssh ${SSH_OPTIONS} "${SSH_TARGET}" "tar -xzf - -C ${REMOTE_DEPLOY_PATH}"

if [ -n "${REMOTE_ENV_FILE:-}" ]; then
  echo "Uploading remote environment file..."
  scp ${SSH_OPTIONS} "${REMOTE_ENV_FILE}" "${SSH_TARGET}:${REMOTE_DEPLOY_PATH}/.env"
fi

echo "Pulling and starting remote stack..."
ssh ${SSH_OPTIONS} "${SSH_TARGET}" "\
  cd ${REMOTE_DEPLOY_PATH} && \
  IMAGE_REPOSITORY_PREFIX=${IMAGE_REPOSITORY_PREFIX} IMAGE_TAG=${IMAGE_TAG} docker compose ${COMPOSE_FILES} pull && \
  IMAGE_REPOSITORY_PREFIX=${IMAGE_REPOSITORY_PREFIX} IMAGE_TAG=${IMAGE_TAG} docker compose ${COMPOSE_FILES} up -d --no-build"

if [ "${RUN_SERVICE_DB_SYNC}" = "true" ]; then
  echo "Running service database sync on remote host..."
  ssh ${SSH_OPTIONS} "${SSH_TARGET}" "\
    cd ${REMOTE_DEPLOY_PATH} && \
    IMAGE_REPOSITORY_PREFIX=${IMAGE_REPOSITORY_PREFIX} IMAGE_TAG=${IMAGE_TAG} docker compose ${COMPOSE_FILES} --profile service-dbs run --rm service-db-sync"
fi

echo "Checking remote application health..."
ssh ${SSH_OPTIONS} "${SSH_TARGET}" "cd ${REMOTE_DEPLOY_PATH} && sh scripts/ci/health-check.sh"

echo "Remote deployment complete."
