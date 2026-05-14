# SwasthyaSetu

Healthcare-oriented microservices platform with a **DevOps / SPE** toolchain aligned to **CSE 816** (GitHub, Jenkins, Docker, Ansible, Kubernetes, ELK).

## Rubric checklist (IIITB CSE 816)

See **`docs/cse816-rubric-mapping.md`** for a line-by-line mapping of course expectations to this repository.

## Quick start

- **Local Docker**: `docker compose up -d --build` then `sh scripts/ci/health-check.sh`
- **ELK overlay**: `docker compose -f docker-compose.yml -f docker-compose.observability.yml up -d` → **`docs/kibana-elk-dashboard.md`**
- **Kubernetes**: **`k8s/README.md`**
- **Jenkins pipeline**: root **`Jenkinsfile`** + **`docs/jenkins-ci-cd.md`**

## Repository layout (DevOps)

| Area | Path |
|------|------|
| CI/CD | `Jenkinsfile`, `scripts/ci/*` |
| Containers | `docker-compose*.yml`, `services/*/Dockerfile`, `swasthya-frontend/`, `doctor-frontend/` |
| Ansible | `ansible/` |
| Kubernetes | `k8s/` |
| Observability | `docker-compose.observability.yml`, `docker/logstash/pipeline/` |
| Vault (demo) | `docker-compose.vault.yml`, `scripts/ci/setup-vault.sh` |
