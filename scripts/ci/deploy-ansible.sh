#!/bin/sh
set -eu

ANSIBLE_INVENTORY="${ANSIBLE_INVENTORY:-${ANSIBLE_INVENTORY_FILE:-}}"
ANSIBLE_SETUP_DOCKER="${ANSIBLE_SETUP_DOCKER:-true}"
ANSIBLE_LIMIT="${ANSIBLE_LIMIT:-}"
REMOTE_DEPLOY_PATH="${REMOTE_DEPLOY_PATH:-swasthya-setu}"
RUN_SERVICE_DB_SYNC="${RUN_SERVICE_DB_SYNC:-false}"
COMPOSE_PROJECT_NAME="${COMPOSE_PROJECT_NAME:-swasthya-setu}"

: "${ANSIBLE_INVENTORY:?ANSIBLE_INVENTORY or ANSIBLE_INVENTORY_FILE is required}"
: "${IMAGE_REPOSITORY_PREFIX:?IMAGE_REPOSITORY_PREFIX is required}"
: "${IMAGE_TAG:?IMAGE_TAG is required}"

require_command() {
  if ! command -v "$1" >/dev/null 2>&1; then
    echo "$1 is required for Ansible deployment."
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

run_playbook() {
  if [ -n "$ANSIBLE_LIMIT" ]; then
    ansible-playbook -i "$ANSIBLE_INVENTORY" --limit "$ANSIBLE_LIMIT" "$@"
  else
    ansible-playbook -i "$ANSIBLE_INVENTORY" "$@"
  fi
}

require_command ansible-playbook
reject_spaces REMOTE_DEPLOY_PATH "$REMOTE_DEPLOY_PATH"
reject_spaces IMAGE_REPOSITORY_PREFIX "$IMAGE_REPOSITORY_PREFIX"
reject_spaces IMAGE_TAG "$IMAGE_TAG"

EXTRA_VARS="deploy_path=$REMOTE_DEPLOY_PATH image_repository_prefix=$IMAGE_REPOSITORY_PREFIX image_tag=$IMAGE_TAG run_service_db_sync=$RUN_SERVICE_DB_SYNC compose_project_name=$COMPOSE_PROJECT_NAME"

if [ "$ANSIBLE_SETUP_DOCKER" = "true" ]; then
  echo "Preparing remote Docker host with Ansible..."
  run_playbook ansible/playbooks/setup-docker.yml
fi

echo "Deploying Compose stack with Ansible..."
run_playbook -e "$EXTRA_VARS" ansible/playbooks/deploy-compose.yml

echo "Ansible deployment complete."
