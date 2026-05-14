# Jenkins CI/CD

This repository uses the root `Jenkinsfile` for build, test, image build, optional registry publishing, and optional deployment. For a **course rubric crosswalk** (CSE 816), see **`docs/cse816-rubric-mapping.md`**.

## Jenkins Agent Requirements

- JDK 21
- Maven 3.9+
- Node.js 22+
- Python 3.12+
- Docker with Docker Compose v2
- Ansible optional, required only for `RUN_ANSIBLE_DEPLOY`
- **`kubectl` required** on the Jenkins agent when you enable **`RUN_K8S_REGISTRY_DEPLOY`** (or use `RUN_MINIKUBE_DEPLOY`). The agent must use a kubeconfig whose default context points at your target cluster (Minikube, lab cluster, or cloud).
- Jenkins plugins: **Pipeline**, **Git**, **GitHub** (for `githubPush()` + `/github-webhook/`), **Credentials Binding**, **SSH Agent**, and **JUnit**

## Create the Pipeline

1. Create a Jenkins Pipeline job.
2. Select **Pipeline script from SCM**.
3. **Git**: repository URL and branch (for example `*/aditya-branch` or `*/main`).
4. Set **Script Path** to `Jenkinsfile`.
5. Under **Build Triggers**, enable **GitHub hook trigger for GITScm polling** (CSE 816).
6. Configure the GitHub webhook using `docs/github-webhook-jenkins.md` (Payload URL must end with `/github-webhook/`). If GitHub cannot reach Jenkins, use **Poll SCM** instead of the webhook.
7. Save the job.

### CSE 816 default pipeline (push → build)

With the current `Jenkinsfile` defaults, a **Git push** runs **Validate → Java tests (`scripts/ci/test-java-services.sh`) → sequential frontends → AI check → publish → local Compose deploy** when **`PUBLISH_IMAGES`** and **`RUN_LOCAL_DEPLOY`** are on. **Minikube, K8s registry, remote SSH, Ansible, and ELK** run only when their parameters are enabled (otherwise those stages are **skipped**). Create Jenkins credentials **`swasthya-dockerhub`** before publishing. For **Kubernetes-only** deploy, turn **`RUN_LOCAL_DEPLOY`** off and **`RUN_K8S_REGISTRY_DEPLOY`** on. Use **`RUN_DOCKER_BUILD`** only for a standalone **`docker compose build`** when **`PUBLISH_IMAGES`** is off (publish already builds images).

For a **tests-only** run (no Hub, no deploy), disable **`PUBLISH_IMAGES`** and **`RUN_LOCAL_DEPLOY`** in **Build with Parameters**.

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

- `RUN_DOCKER_BUILD`: optional `docker compose build` when **not** publishing (skipped when `PUBLISH_IMAGES` is true).
- `RUN_LOCAL_DEPLOY`: runs Docker Compose on the Jenkins host after a successful build.
- `RUN_SERVICE_DB_SYNC`: deploys with `docker-compose.service-dbs.yml` and seeds `auth_db`, `patient_db`, `appointment_db`, and `hospital_db`.
- `PUBLISH_IMAGES`: builds and pushes app/frontend images to a registry using `docker-compose.images.yml`.
- `RUN_MINIKUBE_DEPLOY`: builds images inside Minikube, applies `k8s/`, and runs Kubernetes rollout/health checks.
- `RUN_K8S_REGISTRY_DEPLOY`: deploys the just-published Docker Hub images to the configured Kubernetes context (default off so agents without `kubectl` still pass the pipeline; turn on when the agent has a valid cluster context).
- `RUN_ELK_VERIFICATION`: starts Elasticsearch, Logstash, and Kibana (Compose overlay) and runs ELK health checks plus a GELF smoke test (see `docs/cse816-rubric-mapping.md`).
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

The `Jenkinsfile` defaults target **CSE 816**: **`PUBLISH_IMAGES`** and **`RUN_LOCAL_DEPLOY`** are on. Turn **`PUBLISH_IMAGES`** off for CI-only runs. Enable **`RUN_DOCKER_BUILD`** only when you need a standalone **`docker compose build`** (no registry). Enable **`RUN_LOCAL_DEPLOY`** for Compose on the Jenkins host; enable **`RUN_SERVICE_DB_SYNC`** when you specifically want the service-owned database path.

For registry publishing, enable `PUBLISH_IMAGES` and set `DOCKER_REGISTRY_CREDENTIALS_ID`. For remote deploy, also enable `RUN_REMOTE_DEPLOY` and provide the remote SSH parameters.

For Ansible deploy, enable `PUBLISH_IMAGES` and `RUN_ANSIBLE_DEPLOY`, provide `REMOTE_SSH_CREDENTIALS_ID`, and provide either `ANSIBLE_INVENTORY_PATH` or `ANSIBLE_INVENTORY_FILE_CREDENTIALS_ID`.

For Minikube CD testing, enable `RUN_MINIKUBE_DEPLOY`. The Jenkins agent must have Docker, Minikube, and kubectl access.

For Kubernetes deployment from Docker Hub, enable `PUBLISH_IMAGES` and `RUN_K8S_REGISTRY_DEPLOY`. The Jenkins agent must have `kubectl` configured for the target cluster.

## Jenkins + Kubernetes (typical SPE workflow)

Use this when the course requires **orchestration on Kubernetes** with **Jenkins** driving build, test, image publish, and deploy.

### What runs in Jenkins

1. **Checkout** → **Validate** → **Tests** (Java, frontends, AI check).
2. **`PUBLISH_IMAGES`**: build and push all app images with `scripts/ci/publish-images.sh` (Docker Hub or your `IMAGE_REPOSITORY_PREFIX`).
3. **`RUN_K8S_REGISTRY_DEPLOY`**: `scripts/ci/render-k8s-registry.sh` rewrites image names/tags, then `kubectl apply` deploys to the cluster. Optional rollout health checks run via `scripts/ci/health-check-k8s.sh`.

Do **not** turn on **`RUN_MINIKUBE_DEPLOY`** at the same time as **`RUN_K8S_REGISTRY_DEPLOY`** (the `Jenkinsfile` blocks that combination).

### One-time setup

| Step | Action |
|------|--------|
| Cluster | A reachable Kubernetes cluster (Minikube on the Jenkins box, a lab VM, or a managed cluster). |
| `kubectl` on the agent | Install `kubectl` and use a kubeconfig whose **default context** points at the target cluster. For Minikube on the same machine: `minikube start`, then select that context. |
| Registry | Images must be **pullable** by nodes. Public Docker Hub repos work with the current manifests. Private repos need **`imagePullSecrets`** on Deployments (not in base `k8s/`). |
| Namespace | `k8s/kustomization.yaml` sets namespace **`swasthya-setu`** (matches Jenkins `K8S_NAMESPACE`). |
| Secrets | Edit or replace **`k8s/app-secrets.yaml`** (or create `swasthya-secrets` separately) for DB, JWT, RabbitMQ, and mail before non-local demos. |
| Ingress / DNS | See **`k8s/README.md`**: ingress addon, `swasthya.local` in `/etc/hosts`, etc. |
| HPA | Install **metrics-server** if you use **`k8s/hpa.yaml`**. |

### Jenkins job parameters (Kubernetes CD)

| Parameter | Value for K8s + registry deploy |
|-----------|-----------------------------------|
| `PUBLISH_IMAGES` | **true** |
| `RUN_K8S_REGISTRY_DEPLOY` | **true** |
| `RUN_MINIKUBE_DEPLOY` | **false** |
| `IMAGE_REPOSITORY_PREFIX` | e.g. `docker.io/yourdockerhubuser` (must match pushed images) |
| `IMAGE_TAG` | Leave empty for **Git commit SHA** (first 12 chars), or set a release tag |
| Registry credentials | As in **`docs/dockerhub-setup.md`** |

After a successful deploy, access URLs match **`k8s/README.md`** (ingress host or port-forwards from `health-check-k8s.sh`).

To demonstrate **ELK in CI**, enable **`RUN_ELK_VERIFICATION`** on an agent with enough RAM for Elasticsearch, then open Kibana per **`docs/kibana-elk-dashboard.md`**.

### GitHub webhook

Keep **`githubPush()`** in the `Jenkinsfile` so pushes trigger the pipeline; configure the webhook per **`docs/github-webhook-jenkins.md`**.

### Manual rehearsal (same as the deploy stage)

```bash
IMAGE_REPOSITORY_PREFIX=docker.io/yourdockerhubuser \
IMAGE_TAG=your-tag \
K8S_NAMESPACE=swasthya-setu \
sh scripts/ci/deploy-k8s-registry.sh
```

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

For Kubernetes deploy from Docker Hub images:

```bash
IMAGE_REPOSITORY_PREFIX=docker.io/adityapareek01 \
IMAGE_TAG=local \
sh scripts/ci/deploy-k8s-registry.sh
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
