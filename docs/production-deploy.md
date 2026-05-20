# Production Deploy Phase

This phase adds registry publishing, optional remote Docker Compose deployment, and optional Ansible-based deployment. The pipeline stays safe by default: it only publishes or deploys when the Jenkins build parameters are enabled.

## What Is Automated

- Builds all application, service, AI, and frontend images.
- Tags images through `docker-compose.images.yml`.
- Pushes app images to the configured registry.
- Copies Compose deployment files to a remote server over SSH.
- Pulls the pushed images on the remote server.
- Starts the remote stack with `docker compose up -d --no-build`.
- Optionally runs the service-owned database sync.
- Runs the existing application health check on the remote server.
- Can prepare and deploy the remote Docker host through Ansible playbooks.

## Manual Steps You Must Do Once

1. Choose a registry:
   - Required for the final project demo: Docker Hub.
   - Registry URL: `docker.io`.
   - Image prefix example: `docker.io/adityapareek01`.
2. Create a registry token:
   - For Docker Hub, create a Docker Hub access token.
   - Use that token as the password in Jenkins.
3. Add Jenkins registry credentials:
   - Manage Jenkins -> Credentials -> System -> Global credentials -> Add Credentials.
   - Kind: Username with password.
   - ID: `swasthya-dockerhub`.
4. Create the Jenkins Pipeline job:
   - New Item -> Pipeline.
   - Pipeline definition: Pipeline script from SCM.
   - SCM: Git.
   - Repository URL: this repository.
   - Branch specifier: `*/aditya-branch`.
   - Script Path: `Jenkinsfile`.
5. If deploying to a server, prepare the server:
   - Install Docker and Docker Compose v2.
   - Create a deploy user that can run Docker.
   - Open the required ports, usually `5173`, `5174`, `8080`, and any admin ports you intentionally expose.
   - Add the Jenkins SSH public key to the deploy user's `~/.ssh/authorized_keys`.
6. Add Jenkins SSH credentials, only for remote deploy:
   - Kind: SSH Username with private key.
   - ID: `swasthya-deploy-ssh`.
7. Add a production `.env` secret file, optional but recommended:
   - Kind: Secret file.
   - ID: `swasthya-prod-env`.
   - Use `.env.example` as the template, but replace placeholder passwords and secrets.
8. For Ansible deploy, install Ansible on the Jenkins agent and create an inventory:
   - Use `ansible/inventory.example.ini` as the template.
   - Store private inventories as a Jenkins secret file, for example `swasthya-ansible-inventory`.

## Jenkins Build Parameters For First Publish

- `RUN_DOCKER_BUILD`: false
- `PUBLISH_IMAGES`: true
- `IMAGE_REPOSITORY_PREFIX`: `docker.io/adityapareek01`
- `IMAGE_TAG`: leave empty for Git SHA, or use a release name like `phase-cd-1`
- `DOCKER_REGISTRY_URL`: `docker.io`
- `DOCKER_REGISTRY_CREDENTIALS_ID`: `swasthya-dockerhub`
- `RUN_LOCAL_DEPLOY`: false
- `RUN_REMOTE_DEPLOY`: false

## Jenkins Build Parameters For Remote Deploy

- `PUBLISH_IMAGES`: true
- `RUN_REMOTE_DEPLOY`: true
- `REMOTE_DEPLOY_HOST`: your server IP or domain
- `REMOTE_DEPLOY_USER`: your deploy user
- `REMOTE_DEPLOY_PATH`: `swasthya-setu`
- `REMOTE_SSH_CREDENTIALS_ID`: `swasthya-deploy-ssh`
- `REMOTE_ENV_FILE_CREDENTIALS_ID`: `swasthya-prod-env` if you created it
- `RUN_SERVICE_DB_SYNC`: true only when you want the split service DB seed/sync path

## Jenkins Build Parameters For Ansible Deploy

- `PUBLISH_IMAGES`: true
- `RUN_ANSIBLE_DEPLOY`: true
- `RUN_REMOTE_DEPLOY`: false
- `ANSIBLE_SETUP_DOCKER`: true for first deployment, false after Docker is prepared
- `ANSIBLE_INVENTORY_PATH`: `ansible/inventory.example.ini`, or leave it and use `ANSIBLE_INVENTORY_FILE_CREDENTIALS_ID`
- `ANSIBLE_INVENTORY_FILE_CREDENTIALS_ID`: `swasthya-ansible-inventory` if using a secret inventory
- `REMOTE_DEPLOY_PATH`: `swasthya-setu`
- `REMOTE_SSH_CREDENTIALS_ID`: `swasthya-deploy-ssh`
- `REMOTE_ENV_FILE_CREDENTIALS_ID`: `swasthya-prod-env` if you created it
- `RUN_SERVICE_DB_SYNC`: true only when you want the split service DB seed/sync path

No manual RabbitMQ, Redis, or PostgreSQL accounts are needed for the Compose deployment. Docker Compose creates those containers and credentials from `.env`.
