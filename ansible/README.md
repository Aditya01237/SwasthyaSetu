# Ansible Deployment

This folder adds the configuration-management phase required by the final project. It prepares a Linux Docker host and deploys the same Compose stack that Jenkins already builds and publishes.

## Files

- `inventory.example.ini`: example target inventory.
- `playbooks/setup-docker.yml`: installs Docker and the Docker Compose plugin on Debian/Ubuntu.
- `playbooks/deploy-compose.yml`: copies the Compose bundle, pulls published images, starts the stack, optionally runs service DB sync, and executes the app health check.

## One-Time Target Setup

1. Create a VM or cloud server running Ubuntu/Debian.
2. Create a deploy user.
3. Add the Jenkins SSH public key to the deploy user's `~/.ssh/authorized_keys`.
4. Copy `inventory.example.ini` to a private inventory file and replace `YOUR_SERVER_IP`.

## Local Run

```bash
ANSIBLE_INVENTORY=ansible/inventory.example.ini \
IMAGE_REPOSITORY_PREFIX=ghcr.io/aditya01237/swasthya-setu \
IMAGE_TAG=your-image-tag \
sh scripts/ci/deploy-ansible.sh
```

Set `ANSIBLE_SETUP_DOCKER=false` after the first successful setup run.

## Jenkins Run

Use the `RUN_ANSIBLE_DEPLOY` parameter after `PUBLISH_IMAGES=true`. Provide:

- `REMOTE_SSH_CREDENTIALS_ID`: SSH key for the target host.
- `ANSIBLE_INVENTORY_PATH` or `ANSIBLE_INVENTORY_FILE_CREDENTIALS_ID`.
- `REMOTE_ENV_FILE_CREDENTIALS_ID`, if production secrets should be uploaded as `.env`.

RabbitMQ, Redis, PostgreSQL, and Mailpit do not need manual accounts for this deployment. Compose creates them using `.env` values.
