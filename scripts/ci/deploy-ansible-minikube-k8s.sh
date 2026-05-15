#!/bin/sh
set -eu

MINIKUBE_PROFILE="${MINIKUBE_PROFILE:-minikube}"
K8S_NAMESPACE="${K8S_NAMESPACE:-swasthya-setu}"

: "${IMAGE_REPOSITORY_PREFIX:?IMAGE_REPOSITORY_PREFIX is required}"
: "${IMAGE_TAG:?IMAGE_TAG is required}"

require_command() {
  if ! command -v "$1" >/dev/null 2>&1; then
    echo "$1 is required for local Minikube Ansible deployment."
    exit 1
  fi
}

require_command ansible-playbook
require_command docker
require_command minikube
require_command kubectl

echo "Deploying SwasthyaSetu to local Minikube Kubernetes using Ansible..."
echo "IMAGE_REPOSITORY_PREFIX=$IMAGE_REPOSITORY_PREFIX"
echo "IMAGE_TAG=$IMAGE_TAG"
echo "K8S_NAMESPACE=$K8S_NAMESPACE"
echo "MINIKUBE_PROFILE=$MINIKUBE_PROFILE"

ansible-playbook -i localhost, ansible/playbooks/deploy-local-minikube-k8s.yml

echo "Local Minikube Kubernetes deployment with Ansible complete."