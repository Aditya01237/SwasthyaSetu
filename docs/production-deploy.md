# Production Deploy Phase

This phase adds registry publishing and optional remote Docker Compose deployment. The pipeline stays safe by default: it only publishes or deploys when the Jenkins build parameters are enabled.

## What Is Automated

- Builds all application, service, AI, and frontend images.
- Tags images through `docker-compose.images.yml`.
- Pushes app images to the configured registry.
- Copies Compose deployment files to a remote server over SSH.
- Pulls the pushed images on the remote server.
- Starts the remote stack with `docker compose up -d --no-build`.
- Optionally runs the service-owned database sync.
- Runs the existing application health check on the remote server.

## Manual Steps You Must Do Once

1. Choose a registry:
   - Recommended for GitHub projects: GitHub Container Registry.
   - Registry URL: `ghcr.io`.
   - Image prefix example: `ghcr.io/aditya01237/swasthya-setu`.
2. Create a registry token:
   - For GHCR, create a GitHub token with package write access.
   - Use that token as the password in Jenkins.
3. Add Jenkins registry credentials:
   - Manage Jenkins -> Credentials -> System -> Global credentials -> Add Credentials.
   - Kind: Username with password.
   - ID: `swasthya-registry`.
4. Create the Jenkins Pipeline job:
   - New Item -> Pipeline.
   - Pipeline definition: Pipeline script from SCM.
   - SCM: Git.
   - Repository URL: this repository.
   - Branch specifier: `*/aditya-pareek`.
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

## Jenkins Build Parameters For First Publish

- `RUN_DOCKER_BUILD`: false
- `PUBLISH_IMAGES`: true
- `IMAGE_REPOSITORY_PREFIX`: `ghcr.io/aditya01237/swasthya-setu`
- `IMAGE_TAG`: leave empty for Git SHA, or use a release name like `phase-cd-1`
- `DOCKER_REGISTRY_URL`: `ghcr.io`
- `DOCKER_REGISTRY_CREDENTIALS_ID`: `swasthya-registry`
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

No manual RabbitMQ, Redis, or PostgreSQL accounts are needed for the Compose deployment. Docker Compose creates those containers and credentials from `.env`.
