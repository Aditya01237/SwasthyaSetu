# CSE 816 Final Project — Rubric Mapping (SwasthyaSetu)

This document maps **IIITB CSE 816 Software Production Engineering** expectations to concrete artifacts in this repository.

## Required toolchain

| Expectation | Implementation |
|-------------|----------------|
| **Git and GitHub** | Source hosted on GitHub; branch workflow as configured in Jenkins (`docs/github-webhook-jenkins.md`). |
| **CI/CD — Jenkins, pipeline, Git trigger** | Root `Jenkinsfile` (Declarative Pipeline): checkout → validate → parallel tests → frontends → AI check → optional Docker build/publish → optional deploys. **`githubPush()`** in `triggers` for webhook-driven builds. **GIT SCM polling**: enable **Poll SCM** on the Jenkins job if you need polling without webhooks (see comment in `Jenkinsfile`). |
| **Docker and Docker Compose** | `docker-compose.yml`, `docker-compose.images.yml`, `docker-compose.service-dbs.yml`, `docker-compose.observability.yml`, service `Dockerfile`s. |
| **Ansible** | `ansible/` with **roles** (`common`, `docker`, `app`) and playbooks `setup-docker.yml`, `deploy-compose.yml` (`ansible/README.md`). Jenkins: `RUN_ANSIBLE_DEPLOY`. |
| **Kubernetes** | `k8s/` (Kustomize), ingress, StatefulSet Postgres, Deployments, **`k8s/hpa.yaml`**. Deploy: `scripts/ci/deploy-k8s-registry.sh` or `scripts/ci/deploy-minikube.sh`. Jenkins: `RUN_K8S_REGISTRY_DEPLOY` / `RUN_MINIKUBE_DEPLOY`. |
| **ELK** | `docker-compose.observability.yml`, `docker/logstash/pipeline/logstash.conf` (GELF → Elasticsearch index `swasthya-setu-logs-*`). Jenkins optional stage: **`RUN_ELK_VERIFICATION`** → `scripts/ci/check-elk-observability.sh`. Kibana usage: **`docs/kibana-elk-dashboard.md`**. |

## Mandatory behaviours

1. **Push → Jenkins → build → tests → Docker Hub → deploy**  
   - Webhook / Poll SCM triggers Jenkins.  
   - Tests: `Jenkinsfile` parallel `mvn test`, frontend `npm ci`/`build`, AI `py_compile`.  
   - Registry: `PUBLISH_IMAGES` + `scripts/ci/publish-images.sh`.  
   - Target: **`RUN_K8S_REGISTRY_DEPLOY`**, **`RUN_LOCAL_DEPLOY`**, **`RUN_REMOTE_DEPLOY`**, or **`RUN_ANSIBLE_DEPLOY`** (pick one per pipeline run as appropriate).

2. **Refresh shows new changes**  
   - New image tag (e.g. Git SHA) + redeploy updates workloads.  
   - Kubernetes Deployments use **`RollingUpdate`** with `maxUnavailable: 0` / `maxSurge: 1` for rolling, low-downtime updates (`k8s/app-services.yaml`, `k8s/rabbitmq.yaml`).

3. **Logs in ELK and insights in Kibana**  
   - With observability compose: GELF driver ships container logs to Logstash → Elasticsearch.  
   - Run **`scripts/ci/smoke-elk-log.sh`** (invoked from `check-elk-observability.sh`) or enable **`RUN_ELK_VERIFICATION`** in Jenkins.  
   - Create index pattern / Discover in Kibana per **`docs/kibana-elk-dashboard.md`**.

## Advanced / encouraged (marks)

| Topic | Where |
|-------|--------|
| **Vault** | `docker-compose.vault.yml`, `scripts/ci/setup-vault.sh` (dev-mode Vault + KV paths). Integrate with production secrets workflow as needed. |
| **Ansible roles** | `ansible/roles/*` |
| **Kubernetes HPA** | `k8s/hpa.yaml` (requires metrics-server) |
| **Live patching / zero downtime** | Rolling updates on Deployments (above); avoid `Recreate` for app tiers during demos. |
| **Domain** | Healthcare-oriented microservices (patient, appointment, hospital, auth, AI, etc.). |

## Quick links

- Jenkins + Kubernetes: `docs/jenkins-ci-cd.md`  
- Kibana steps: `docs/kibana-elk-dashboard.md`  
- Kubernetes local: `k8s/README.md`  
- Docker Hub + Jenkins credentials: `docs/dockerhub-setup.md`
