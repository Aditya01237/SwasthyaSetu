# SwasthyaSetu

**SwasthyaSetu** means **Health Bridge**. It is a healthcare appointment and patient-management platform built using a microservices architecture and a complete DevOps workflow.

The project demonstrates how a real-world healthcare system can be built, containerized, tested, deployed to Kubernetes, secured using Vault, and monitored using ELK/Kibana.

---

## Table of Contents

1. [Project Overview](#project-overview)
2. [Problem Statement](#problem-statement)
3. [Key Features](#key-features)
4. [Architecture](#architecture)
5. [Microservices](#microservices)
6. [Technology Stack](#technology-stack)
7. [Database Design](#database-design)
8. [DevOps Workflow](#devops-workflow)
9. [Repository Structure](#repository-structure)
10. [Prerequisites](#prerequisites)
11. [Local Kubernetes Deployment](#local-kubernetes-deployment)
12. [Vault SMTP Setup](#vault-smtp-setup)
13. [Demo Database Seeding](#demo-database-seeding)
14. [Application Access](#application-access)
15. [Jenkins Pipeline](#jenkins-pipeline)
16. [Observability](#observability)
17. [Useful Commands](#useful-commands)
18. [Troubleshooting](#troubleshooting)
19. [Viva Explanation](#viva-explanation)

---

## Project Overview

SwasthyaSetu is a hospital appointment booking and healthcare access platform. It connects patients, doctors, hospital administration, and backend services through a unified system.

The project focuses on both software functionality and DevOps practices:

- Microservices-based backend
- Patient and doctor frontend applications
- PostgreSQL, Redis, and RabbitMQ infrastructure
- Docker-based containerization
- Jenkins CI/CD pipeline
- Kubernetes deployment on Minikube
- Ansible-based deployment automation
- HashiCorp Vault for secrets
- ELK stack for log monitoring

---

## Problem Statement

Healthcare systems are often fragmented. Patients may need to call hospitals, manually track appointments, carry physical records, and wait in inefficient queues.

SwasthyaSetu solves this by providing a single platform where:

- Patients can register and authenticate using OTP.
- Patients can browse doctors and hospitals.
- Appointments can be booked digitally.
- Services communicate through APIs and asynchronous messaging.
- Secrets are managed securely.
- Logs and deployment status can be monitored.

---

## Key Features

### Patient Side

- Patient registration
- OTP-based login
- Browse hospitals and doctors
- Appointment booking
- View appointment-related information

### Doctor / Hospital Side

- Doctor frontend
- Hospital and doctor data management
- Doctor appointment visibility

### DevOps Features

- CI/CD pipeline using Jenkins
- Docker image build and push to Docker Hub
- Kubernetes deployment using Ansible
- Vault-based SMTP secret injection
- RabbitMQ-based asynchronous notification flow
- Redis support for fast appointment-related operations
- ELK stack for centralized logging
- Minikube local cluster deployment

---

## Architecture

High-level request flow:

```text
Patient / Doctor Frontend
        |
        v
API Gateway
        |
        +--> auth-service
        +--> patient-service
        +--> hospital-service
        +--> appointment-service
        +--> backend
        +--> ai-service
        |
        v
RabbitMQ / Redis / PostgreSQL
        |
        v
notification-service -> Gmail SMTP OTP
```

DevOps flow:

```text
Developer Pushes Code to GitHub
        |
        v
Jenkins Pipeline
        |
        +--> Validate Config
        +--> Run Tests
        +--> Build Frontends
        +--> Check AI Service
        +--> Start / Seed Vault
        +--> Build Docker Images
        +--> Push Images to Docker Hub
        +--> Deploy to Minikube using Ansible
        +--> Apply SMTP Secrets from Vault
        |
        v
Kubernetes Cluster
```

---

## Microservices

| Service | Purpose |
|---|---|
| `api-gateway` | Entry point for frontend requests and route forwarding |
| `auth-service` | OTP login and authentication-related operations |
| `patient-service` | Patient registration and patient data |
| `hospital-service` | Hospital and doctor-related data |
| `appointment-service` | Appointment booking and appointment flow |
| `notification-service` | Consumes events and sends OTP/email notifications |
| `backend` | Legacy/backend transition service |
| `ai-service` | Python AI service used by the platform |
| `swasthya-frontend` | Patient frontend |
| `doctor-frontend` | Doctor frontend |

---

## Technology Stack

### Backend

- Java Spring Boot
- Spring Web
- Spring Data JPA
- Spring Security / JWT
- Spring AMQP
- PostgreSQL
- Redis
- RabbitMQ

### Frontend

- React
- Vite
- Axios
- Tailwind / CSS styling

### AI Service

- Python
- FastAPI-style lightweight service structure

### DevOps

- GitHub
- Jenkins
- Docker
- Docker Compose
- Docker Hub
- Kubernetes
- Minikube
- Ansible
- HashiCorp Vault
- ELK Stack: Elasticsearch, Logstash, Kibana

---

## Database Design

The project uses **one PostgreSQL pod/container**, but inside it there are **multiple logical databases**.

```text
One PostgreSQL instance
        |
        +--> swasthyasetudb
        +--> auth_db
        +--> patient_db
        +--> appointment_db
        +--> hospital_db
```

Service-to-database mapping:

| Service | Database |
|---|---|
| `backend` | `swasthyasetudb` |
| `auth-service` | `auth_db` |
| `patient-service` | `patient_db` |
| `appointment-service` | `appointment_db` |
| `hospital-service` | `hospital_db` |

This means we are not running a separate PostgreSQL container for every service. Instead, we run one PostgreSQL StatefulSet and create multiple service-specific databases inside it.

---

## DevOps Workflow

SwasthyaSetu implements the following DevOps lifecycle:

1. Developer pushes code to GitHub.
2. Jenkins pipeline is triggered.
3. Jenkins validates configuration.
4. Jenkins runs Java service tests.
5. Jenkins builds frontend applications.
6. Jenkins checks the Python AI service.
7. Vault is started and seeded with registry/app secrets.
8. Docker images are built.
9. Docker images are pushed to Docker Hub.
10. Ansible deploys Kubernetes manifests to Minikube.
11. SMTP secrets are read from Vault and applied as Kubernetes Secret.
12. Kubernetes restarts `notification-service` so it receives SMTP credentials.

---

## Repository Structure

```text
SwasthyaSetu/
|
|-- Jenkinsfile
|-- docker-compose.yml
|-- docker-compose.vault.yml
|-- docker-compose.observability.yml
|-- dummy_data.sql
|
|-- services/
|   |-- api-gateway/
|   |-- auth-service/
|   |-- patient-service/
|   |-- hospital-service/
|   |-- appointment-service/
|   |-- notification-service/
|   |-- backend/
|   |-- ai-service/
|
|-- swasthya-frontend/
|-- doctor-frontend/
|
|-- k8s/
|   |-- namespace.yaml
|   |-- app-config.yaml
|   |-- app-secrets.yaml
|   |-- postgres.yaml
|   |-- redis.yaml
|   |-- rabbitmq.yaml
|   |-- app-services.yaml
|   |-- frontends.yaml
|   |-- ingress.yaml
|   |-- hpa.yaml
|   |-- kustomization.yaml
|
|-- ansible/
|   |-- playbooks/
|       |-- deploy-local-minikube-k8s.yml
|
|-- scripts/
|   |-- ci/
|   |-- local/
|
|-- docker/
|   |-- postgres/
|   |-- logstash/
|
|-- docs/
```

---

## Prerequisites

Install the following tools:

- Git
- Docker Desktop
- Docker Compose
- Java 17+
- Maven
- Node.js and npm
- Python 3
- Jenkins
- kubectl
- Minikube
- Ansible
- Vault CLI

For Mac M-series systems, Docker Desktop resources should be increased before starting Minikube.

Recommended Docker Desktop resources:

```text
CPU: 6 or more
Memory: 10 GB or more
Swap: 2 GB or more
Disk: 60 GB or more
```

---

## Local Kubernetes Deployment

Clone the repository:

```bash
git clone https://github.com/Aditya01237/SwasthyaSetu.git
cd SwasthyaSetu
git checkout aditya-branch
```

Start Minikube manually if needed:

```bash
minikube delete
minikube start --driver=docker --cpus=6 --memory=9000
minikube addons enable ingress
minikube addons enable metrics-server
```

Check cluster:

```bash
kubectl get nodes
```

The Jenkins/Ansible deployment can also start Minikube automatically if it is not running.

---

## Vault SMTP Setup

Vault is used for SMTP credentials so Gmail credentials are not hardcoded into Kubernetes YAML files.

Start Vault:

```bash
export VAULT_PORT=18200
export VAULT_DEV_ROOT_TOKEN=swasthya-root-token

docker compose -f docker-compose.yml -f docker-compose.vault.yml up -d vault
```

Set Vault environment:

```bash
export VAULT_ADDR=http://localhost:18200
export VAULT_TOKEN=swasthya-root-token
```

Check Vault:

```bash
vault status
```

Add SMTP credentials:

```bash
vault kv put secret/swasthya-setu/smtp \
  SPRING_MAIL_USERNAME="your_email@gmail.com" \
  SPRING_MAIL_PASSWORD="your_gmail_app_password" \
  SPRING_MAIL_HOST="smtp.gmail.com" \
  SPRING_MAIL_PORT="587" \
  SPRING_MAIL_PROPERTIES_MAIL_SMTP_AUTH="true" \
  SPRING_MAIL_PROPERTIES_MAIL_SMTP_STARTTLS_ENABLE="true" \
  APP_NOTIFICATION_FROM="your_email@gmail.com"
```

Verify safely:

```bash
vault kv get -field=SPRING_MAIL_USERNAME secret/swasthya-setu/smtp
vault kv get -field=SPRING_MAIL_HOST secret/swasthya-setu/smtp
vault kv get -field=SPRING_MAIL_PASSWORD secret/swasthya-setu/smtp | wc -c
```

Important:

```text
Vault is running in dev/in-memory mode for local demo.
If the Vault container restarts, manually inserted SMTP secrets may be lost.
Add the SMTP secret again before running Jenkins if Vault was recreated.
```

---

## Demo Database Seeding

After deleting and recreating Minikube, PostgreSQL starts fresh. The old patient data will be gone.

Use the database seed script after Jenkins deployment:

```bash
./scripts/local/seed-demo-database.sh
```

This script:

1. Waits for `postgres-0`.
2. Scales down DB-dependent services.
3. Restarts Postgres to clear old connections.
4. Loads `dummy_data.sql` into `swasthyasetudb`.
5. Runs `sync-service-databases.sh`.
6. Copies demo data into service databases.
7. Starts services again.

Demo UHID:

```text
UHID-987654321
```

Test OTP:

```bash
curl -i --max-time 30 -X POST http://localhost:8081/api/auth/send-otp \
  -H "Content-Type: application/json" \
  -d '{"uhid":"UHID-987654321"}'
```

---

## Application Access

After deployment, run:

```bash
./scripts/local/port-forward-demo.sh
```

Then open:

| Application | URL |
|---|---|
| Patient Frontend | `http://localhost:3004/patient/` |
| Doctor Frontend | `http://localhost:3003/doctor/` |
| API Gateway | `http://localhost:8081` |

Health check:

```bash
curl -i http://localhost:8081/actuator/health
```

---

## Jenkins Pipeline

Jenkins pipeline file:

```text
Jenkinsfile
```

Main stages:

1. Checkout
2. Validate Config
3. Test Java Services
4. Build Frontends
5. Check AI Service
6. HashiCorp Vault
7. Build Docker Images
8. Publish Docker Images
9. Deploy Local Minikube With Ansible
10. Apply SMTP Secrets From Vault

Recommended Jenkins build parameters:

| Parameter | Example |
|---|---|
| `IMAGE_REPOSITORY_PREFIX` | `docker.io/adityapareek01` |
| `IMAGE_TAG` | Leave empty to use Git SHA |
| `DOCKER_REGISTRY_URL` | `docker.io` |
| `DOCKER_REGISTRY_CREDENTIALS_ID` | `swasthya-dockerhub` |

Required Jenkins credentials:

| Credential ID | Type | Purpose |
|---|---|---|
| `swasthya-dockerhub` | Username with password | Docker Hub login |
| `swasthya-vault-token` | Secret text | Vault root token for local demo |

---

## Observability

The project supports ELK-based logging using:

```text
docker-compose.observability.yml
```

Start ELK overlay:

```bash
docker compose -f docker-compose.yml -f docker-compose.observability.yml up -d
```

ELK components:

| Tool | Purpose |
|---|---|
| Elasticsearch | Stores logs |
| Logstash | Processes logs |
| Kibana | Visualizes logs |

Kubernetes metrics-server is enabled for:

```bash
kubectl top pods
kubectl top node
```

---

## Useful Commands

Check pods:

```bash
kubectl get pods -n swasthya-setu
```

Check deployments:

```bash
kubectl get deployments -n swasthya-setu
```

Check services:

```bash
kubectl get svc -n swasthya-setu
```

Check pod resource usage:

```bash
kubectl top pods -n swasthya-setu
kubectl top node
```

Restart a deployment:

```bash
kubectl rollout restart deployment/notification-service -n swasthya-setu
```

Wait for rollout:

```bash
kubectl rollout status deployment/notification-service -n swasthya-setu --timeout=900s
```

View logs:

```bash
kubectl logs -n swasthya-setu deployment/notification-service --tail=150
```

Check SMTP env safely:

```bash
kubectl exec -n swasthya-setu deployment/notification-service -- sh -c '
echo "USERNAME=$SPRING_MAIL_USERNAME"
echo "HOST=$SPRING_MAIL_HOST"
echo "PORT=$SPRING_MAIL_PORT"
echo "FROM=$APP_NOTIFICATION_FROM"
if [ -n "$SPRING_MAIL_PASSWORD" ]; then echo "PASSWORD_EXISTS"; else echo "PASSWORD_MISSING"; fi
'
```

---

## Troubleshooting

### 1. Vault says no value found

Error:

```text
No value found at secret/data/swasthya-setu/smtp
```

Reason:

```text
Vault dev mode uses in-memory storage. Secret was lost after Vault restart.
```

Fix:

```bash
vault kv put secret/swasthya-setu/smtp \
  SPRING_MAIL_USERNAME="your_email@gmail.com" \
  SPRING_MAIL_PASSWORD="your_gmail_app_password" \
  SPRING_MAIL_HOST="smtp.gmail.com" \
  SPRING_MAIL_PORT="587" \
  SPRING_MAIL_PROPERTIES_MAIL_SMTP_AUTH="true" \
  SPRING_MAIL_PROPERTIES_MAIL_SMTP_STARTTLS_ENABLE="true" \
  APP_NOTIFICATION_FROM="your_email@gmail.com"
```

### 2. OTP says Patient not found

Reason:

```text
After deleting Minikube, PostgreSQL data is fresh.
The UHID may not exist in auth_db.
```

Fix:

```bash
./scripts/local/seed-demo-database.sh
```

Then use:

```text
UHID-987654321
```

### 3. Postgres says too many clients already

Reason:

```text
Multiple Spring Boot services opened many DB connections.
```

Fix:

```bash
kubectl scale deployment backend auth-service patient-service appointment-service hospital-service -n swasthya-setu --replicas=0
kubectl delete pod postgres-0 -n swasthya-setu
```

Then run database seed script.

### 4. Notification service rollout timeout

Reason:

```text
Local Minikube is resource-limited. Spring Boot startup can be slow.
```

Fix:

```bash
kubectl rollout status deployment/notification-service -n swasthya-setu --timeout=900s
```

### 5. Docker Desktop memory error

Error:

```text
Docker Desktop has only 9937MB memory but you specified 10000MB
```

Fix:

Use:

```bash
minikube start --driver=docker --cpus=6 --memory=9000
```

or increase Docker Desktop memory from settings.

---

## Viva Explanation

Short explanation:

```text
SwasthyaSetu is a healthcare appointment booking platform built using microservices. It has separate services for authentication, patients, hospitals, appointments, notifications, API gateway, backend, AI service, and frontends. Each service is containerized using Docker and deployed to Kubernetes using Jenkins and Ansible.
```

DevOps explanation:

```text
When code is pushed to GitHub, Jenkins validates the configuration, runs tests, builds frontend and backend images, pushes them to Docker Hub, and deploys them to a local Minikube Kubernetes cluster using Ansible. Vault is used to store SMTP secrets, which are injected into Kubernetes as a Secret and used by notification-service to send OTP emails.
```

Database explanation:

```text
We run one PostgreSQL StatefulSet in Kubernetes. Inside that single PostgreSQL instance, we create multiple logical databases such as auth_db, patient_db, appointment_db, hospital_db, and swasthyasetudb. Each service connects to its own database using JDBC URLs from the Kubernetes ConfigMap.
```

RabbitMQ explanation:

```text
RabbitMQ is used for asynchronous communication. For example, auth-service can publish OTP events, and notification-service consumes those events and sends emails. This decouples authentication from notification delivery.
```

Vault explanation:

```text
Vault stores sensitive SMTP credentials. Jenkins reads SMTP values from Vault, creates a Kubernetes Secret called smtp-secret, injects it into notification-service, and restarts the deployment so the service can send OTP emails.
```

Kubernetes explanation:

```text
Kubernetes manages all services as deployments and runs infrastructure components like PostgreSQL, Redis, and RabbitMQ. Services communicate internally using Kubernetes service names, and frontend/API access is done using port forwarding or ingress.
```

---

## Final Demo Checklist

Before demo:

```bash
export VAULT_ADDR=http://localhost:18200
export VAULT_TOKEN=swasthya-root-token
vault kv get secret/swasthya-setu/smtp
```

Run Jenkins pipeline.

After Jenkins success:

```bash
./scripts/local/seed-demo-database.sh
./scripts/local/port-forward-demo.sh
```

Test OTP:

```bash
curl -i --max-time 30 -X POST http://localhost:8081/api/auth/send-otp \
  -H "Content-Type: application/json" \
  -d '{"uhid":"UHID-987654321"}'
```

---

## Contributors

- Aditya Pareek
- Project team members

---

## License

This repository is created for academic and demonstration purposes as part of the Software Production Engineering / DevOps project.
