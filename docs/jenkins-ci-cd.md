# Jenkins CI/CD

This repository uses the root `Jenkinsfile` for build, test, image build, optional registry publishing, and optional deployment.

## Jenkins Agent Requirements

- JDK 21
- Maven 3.9+
- Node.js 22+
- Python 3.12+
- Docker with Docker Compose v2
- Ansible optional, required only for `RUN_ANSIBLE_DEPLOY`
- `kubectl` optional, used only to render the local Kubernetes manifests
- Jenkins plugins: Pipeline, Git, Credentials Binding, SSH Agent, and JUnit

## Create the Pipeline

1. Create a Jenkins Pipeline job.
2. Select Pipeline script from SCM.
3. Use this repository URL and the `aditya-branch` branch.
4. Set Script Path to `Jenkinsfile`.

## Manual Jenkins Setup

Create these credentials in Jenkins before enabling publishing or remote deploy:

1. Docker registry credentials:
   - Kind: Username with password.
   - ID example: `swasthya-dockerhub`.
   - Username: your Docker Hub username.
   - Password: a Docker Hub access token.
2. Remote SSH credentials, only if using remote deploy:
   - Kind: SSH Username with private key.
   - ID example: `swasthya-deploy-ssh`.
   - Username: the remote Linux deploy user.
   - Private key: a key accepted by the remote server.
3. Remote `.env` file, optional:
   - Kind: Secret file.
   - ID example: `swasthya-prod-env`.
   - File content: production-safe values based on `.env.example`.
4. Ansible inventory file, optional:
   - Kind: Secret file.
   - ID example: `swasthya-ansible-inventory`.
   - File content: an inventory based on `ansible/inventory.example.ini`.

## Pipeline Parameters

- `RUN_DOCKER_BUILD`: builds all service and frontend Docker images after tests pass.
- `RUN_LOCAL_DEPLOY`: runs Docker Compose on the Jenkins host after a successful build.
- `RUN_SERVICE_DB_SYNC`: deploys with `docker-compose.service-dbs.yml` and seeds `auth_db`, `patient_db`, `appointment_db`, and `hospital_db`.
- `PUBLISH_IMAGES`: builds and pushes app/frontend images to a registry using `docker-compose.images.yml`.
- `RUN_MINIKUBE_DEPLOY`: builds images inside Minikube, applies `k8s/`, and runs Kubernetes rollout/health checks.
- `RUN_ANSIBLE_DEPLOY`: deploys published images to a remote Docker host with Ansible.
- `ANSIBLE_SETUP_DOCKER`: installs and starts Docker on the target before Ansible deployment.
- `IMAGE_REPOSITORY_PREFIX`: image prefix, for example `docker.io/adityapareek01`.
- `IMAGE_TAG`: image tag. Leave blank to use the Git commit SHA.
- `DOCKER_REGISTRY_URL`: registry login host. Use `docker.io` for Docker Hub.
- `DOCKER_REGISTRY_CREDENTIALS_ID`: Jenkins credentials ID for registry login.
- `RUN_REMOTE_DEPLOY`: deploys the pushed images to a remote Docker host over SSH.
- `REMOTE_DEPLOY_HOST`: remote server hostname or IP.
- `REMOTE_DEPLOY_USER`: remote SSH user.
- `REMOTE_DEPLOY_PATH`: directory on the remote server where Compose files are stored.
- `REMOTE_SSH_CREDENTIALS_ID`: Jenkins SSH private key credentials ID.
- `REMOTE_ENV_FILE_CREDENTIALS_ID`: optional Jenkins secret file credentials ID copied to `.env` on the remote server.
- `ANSIBLE_INVENTORY_PATH`: repository inventory path when not using a secret inventory file.
- `ANSIBLE_INVENTORY_FILE_CREDENTIALS_ID`: optional Jenkins secret file credentials ID for Ansible inventory.

For normal CI, keep only `RUN_DOCKER_BUILD` enabled. For local CD testing on a Jenkins machine with Docker, enable `RUN_LOCAL_DEPLOY`. Enable `RUN_SERVICE_DB_SYNC` when you specifically want to test the service-owned database path.

For registry publishing, enable `PUBLISH_IMAGES` and set `DOCKER_REGISTRY_CREDENTIALS_ID`. For remote deploy, also enable `RUN_REMOTE_DEPLOY` and provide the remote SSH parameters.

For Ansible deploy, enable `PUBLISH_IMAGES` and `RUN_ANSIBLE_DEPLOY`, provide `REMOTE_SSH_CREDENTIALS_ID`, and provide either `ANSIBLE_INVENTORY_PATH` or `ANSIBLE_INVENTORY_FILE_CREDENTIALS_ID`.

For Minikube CD testing, enable `RUN_MINIKUBE_DEPLOY`. The Jenkins agent must have Docker, Minikube, and kubectl access.

Jenkins local deploy uses CI-only host ports so it can run beside your normal local Docker stack:

- Patient frontend: `http://localhost:15173/patient/`
- Doctor frontend: `http://localhost:15174/doctor/`
- API gateway: `http://localhost:18080`
- AI service: `http://localhost:18000`
- Mailpit: `http://localhost:18025`
- RabbitMQ management: `http://localhost:15674`

## Local Equivalent Commands

```bash
sh scripts/ci/validate-config.sh
sh scripts/ci/test-java-services.sh
sh scripts/ci/build-frontends.sh
PYTHONPYCACHEPREFIX=.pycache python3 -m py_compile services/ai-service/app.py
docker compose build
```

For image publishing:

```bash
IMAGE_REPOSITORY_PREFIX=docker.io/adityapareek01 IMAGE_TAG=local sh scripts/ci/publish-images.sh
```

For a local deploy:

```bash
RUN_SERVICE_DB_SYNC=false sh scripts/ci/deploy-compose.sh
```

For split database deploy and seed:

```bash
RUN_SERVICE_DB_SYNC=true sh scripts/ci/deploy-compose.sh
```

For Minikube deploy:

```bash
sh scripts/ci/deploy-minikube.sh
```

For remote deploy after images have been pushed:

```bash
IMAGE_REPOSITORY_PREFIX=docker.io/adityapareek01 \
IMAGE_TAG=local \
REMOTE_DEPLOY_HOST=your-server.example.com \
REMOTE_DEPLOY_USER=deploy \
REMOTE_DEPLOY_PATH=swasthya-setu \
sh scripts/ci/deploy-remote-compose.sh
```

For Ansible deploy after images have been pushed:

```bash
ANSIBLE_INVENTORY=ansible/inventory.example.ini \
IMAGE_REPOSITORY_PREFIX=docker.io/adityapareek01 \
IMAGE_TAG=local \
REMOTE_DEPLOY_PATH=swasthya-setu \
sh scripts/ci/deploy-ansible.sh
```

See `docs/dockerhub-setup.md` for the exact Docker Hub account/token and Jenkins credential steps.
