# Jenkins CI/CD

This repository uses the root `Jenkinsfile` for build, test, image build, and optional local deployment.

## Jenkins Agent Requirements

- JDK 21
- Maven 3.9+
- Node.js 22+
- Python 3.12+
- Docker with Docker Compose v2
- `kubectl` optional, used only to render the local Kubernetes manifests

## Create the Pipeline

1. Create a Jenkins Pipeline job.
2. Select Pipeline script from SCM.
3. Use this repository URL and the `aditya-pareek` branch.
4. Set Script Path to `Jenkinsfile`.

## Pipeline Parameters

- `RUN_DOCKER_BUILD`: builds all service and frontend Docker images after tests pass.
- `RUN_LOCAL_DEPLOY`: runs Docker Compose on the Jenkins host after a successful build.
- `RUN_SERVICE_DB_SYNC`: deploys with `docker-compose.service-dbs.yml` and seeds `auth_db`, `patient_db`, `appointment_db`, and `hospital_db`.

For normal CI, keep only `RUN_DOCKER_BUILD` enabled. For local CD testing on a Jenkins machine with Docker, enable `RUN_LOCAL_DEPLOY`. Enable `RUN_SERVICE_DB_SYNC` when you specifically want to test the service-owned database path.

## Local Equivalent Commands

```bash
sh scripts/ci/validate-config.sh
sh scripts/ci/test-java-services.sh
sh scripts/ci/build-frontends.sh
PYTHONPYCACHEPREFIX=.pycache python3 -m py_compile services/ai-service/app.py
docker compose build
```

For a local deploy:

```bash
RUN_SERVICE_DB_SYNC=false sh scripts/ci/deploy-compose.sh
```

For split database deploy and seed:

```bash
RUN_SERVICE_DB_SYNC=true sh scripts/ci/deploy-compose.sh
```
