# SwasthyaSetu Microservices Migration

This branch starts the migration with container-ready configuration before extracting Java code into separate services.

## Target Services

| Service | Port | Responsibility |
| --- | ---: | --- |
| api-gateway | 8080 | Route `/api/*`, JWT validation, rate limiting |
| auth-service | 8081 | OTP, doctor login, JWT generation |
| patient-service | 8082 | Patient profile, records, prescription upload, QR audit |
| appointment-service | 8083 | Booking, slot locking, QR token generation/scanning |
| hospital-service | 8084 | Hospitals, doctor catalog, doctor profiles |
| notification-service | 8086 | Email consumers for RabbitMQ events |
| ai-service | 8000 | OCR and prescription parsing |

## Current Branch Scope

- Moved backend-side applications into `services/` for easier navigation:
  `services/backend`, `services/api-gateway`, `services/auth-service`, `services/patient-service`,
  `services/appointment-service`, `services/hospital-service`, `services/notification-service`, and `services/ai-service`.
- Externalized backend DB, mail, JWT, CORS, and AI-service URLs.
- Added Spring Actuator health endpoint for Docker/Kubernetes probes.
- Added `/health` to the FastAPI AI service.
- Added Dockerfiles for backend, AI service, and both frontends.
- Added Docker Compose with PostgreSQL, Redis, RabbitMQ, AI service, current backend, and both frontends.
- Created local logical databases for the future service split: `auth_db`, `patient_db`, `appointment_db`, and `hospital_db`.
- Added `api-gateway` on port `8080` as the first routing layer. During transition it routes all `/api/*` traffic to the current monolith on port `8081`.
- Added `auth-service` on port `8081` and routed `/api/auth/**` to it through the gateway. During the transition it reads the existing patient/doctor/hospital tables from `swasthyasetudb` and stores OTP state in `auth_otp_verification`; once patient/hospital services are extracted, this can move fully to `auth_db`.
- Added `hospital-service` on port `8084` for hospital and doctor catalog endpoints. During the transition it uses the existing hospital/doctor tables in `swasthyasetudb`; later this can move to `hospital_db`.
- Added `appointment-service` on port `8083` for appointment booking, doctor schedule, patient appointment details, and QR scanning. It uses Redis slot locks with keys like `slot:{doctorId}:{appointmentTime}` and keeps the existing DB tables during the transition.
- Added `patient-service` on port `8082` for patient registration, profile history, QR audit logs, and prescription uploads. The gateway now forwards validated JWT claims as `X-User-Id` and `X-User-Role` headers; `/api/patient/register` remains public while other patient routes are protected.
- Added `notification-service` on port `8086` with RabbitMQ consumers for `appointment.booked`, `patient.registered`, and `auth.otp-requested`. Appointment confirmation email now publishes from `appointment-service`; patient welcome email publishes from `patient-service`; OTP email publishes from `auth-service`.
- Added local Kubernetes manifests under `k8s/` for Minikube: app Deployments, ClusterIP Services, NGINX Ingress, PostgreSQL StatefulSet with PVC, Redis, RabbitMQ, and Mailpit.
- Added service-owned database wiring. Normal Docker Compose still uses the shared transition DB so the app stays stable; `docker-compose.service-dbs.yml` switches auth, patient, appointment, and hospital services to `auth_db`, `patient_db`, `appointment_db`, and `hospital_db` for split-DB testing.
- Added `docker/postgres/sync-service-databases.sh` to copy existing transition data into service-owned databases after service schemas have been created; appointment DB seeding now excludes patient clinical tables.
- Added RabbitMQ read-model sync consumers so services can update their own local copies from domain events:
  `patient.registered`, `hospital.upserted`, `doctor.registered`, and `appointment.booked`.
- Expanded event payloads with the IDs and display fields needed for service-owned DB mode. Auth, appointment, patient, and hospital services now declare their own consumer queues for the data they need.
- Started service-to-service boundary cleanup: QR scans and appointment-detail medical-record reads in `appointment-service`
  now call internal `patient-service` endpoints instead of writing/reading patient-owned data directly.
- Patient prescription upload now requires an appointment-specific QR scan audit entry, so the backend enforces the same
  unlock rule the UI expects.
- Removed stale appointment-service JPA ownership of patient audit logs, medical records, and medicines; appointment-service
  now keeps only response DTOs for data returned by patient-service.
- Added Jenkins CI/CD scaffolding with a root `Jenkinsfile`, reusable `scripts/ci/*` checks, optional Docker Compose deploy,
  optional service-owned DB sync, and setup notes in `docs/jenkins-ci-cd.md`.
- Added optional ELK observability with `docker-compose.observability.yml`, Logstash GELF ingestion for service container
  logs, Kibana/Elasticsearch local ports, and setup notes in `docs/observability-elk.md`.
- Added production publishing/deploy scaffolding with `docker-compose.images.yml`, registry push and remote SSH deploy
  scripts, expanded Jenkins parameters, and manual setup notes in `docs/production-deploy.md`.
- Aligned image publishing defaults and setup notes with Docker Hub for the final project requirement.
- Added scripted Minikube deployment and Kubernetes health checks through `scripts/ci/deploy-minikube.sh` and
  `scripts/ci/health-check-k8s.sh`; Jenkins can now run the Minikube deploy path with `RUN_MINIKUBE_DEPLOY`.
- Added Ansible configuration-management deployment with `ansible/playbooks/setup-docker.yml`,
  `ansible/playbooks/deploy-compose.yml`, `scripts/ci/deploy-ansible.sh`, and Jenkins `RUN_ANSIBLE_DEPLOY`.
- Added the Jenkins GitHub push trigger plus setup notes in `docs/github-webhook-jenkins.md`.
- Added Docker Hub image deployment for Kubernetes with `scripts/ci/render-k8s-registry.sh`,
  `scripts/ci/deploy-k8s-registry.sh`, and Jenkins `RUN_K8S_REGISTRY_DEPLOY`.

## Local Run

Create a local env file from the example and fill secrets:

```bash
cp .env.example .env
docker compose up --build
```

To test the service-owned databases:

```bash
docker compose -f docker-compose.yml -f docker-compose.service-dbs.yml up --build -d
docker compose -f docker-compose.yml -f docker-compose.service-dbs.yml --profile service-dbs run --rm service-db-sync
```

The sync command copies the existing transition data from `swasthyasetudb` into the service databases. RabbitMQ consumers now keep new patient, hospital, doctor, and appointment read-model changes flowing after that seed step.

To run centralized local logging with ELK:

```bash
docker compose -f docker-compose.yml -f docker-compose.observability.yml up --build -d
sh scripts/ci/health-check-observability.sh
```

To deploy into Minikube:

```bash
sh scripts/ci/deploy-minikube.sh
```

To deploy published images with Ansible:

```bash
ANSIBLE_INVENTORY=ansible/inventory.example.ini \
IMAGE_REPOSITORY_PREFIX=docker.io/adityapareek01 \
IMAGE_TAG=local \
sh scripts/ci/deploy-ansible.sh
```

Useful URLs:

- Patient frontend: `http://localhost:5173/patient/`
- Doctor frontend: `http://localhost:5174/doctor/`
- Gateway health: `http://localhost:8080/actuator/health`
- Auth service health: `http://localhost:8081/actuator/health`
- Patient service health: `http://localhost:8082/actuator/health`
- Appointment service health: `http://localhost:8083/actuator/health`
- Hospital service health: `http://localhost:8084/actuator/health`
- Notification service health: `http://localhost:8086/actuator/health`
- Transition backend health: `http://localhost:8090/actuator/health`
- AI health: `http://localhost:8000/health`
- RabbitMQ UI: `http://localhost:15672`
- Local email inbox: `http://localhost:8025`
- Kibana: `http://localhost:5601`
- Elasticsearch health: `http://localhost:9200/_cluster/health`

## Next Extraction Order

1. Add production hardening once a real environment is chosen: managed secrets, HTTPS/TLS, domain DNS, backups, and Kubernetes registry image promotion.
2. Add Kubernetes observability manifests or Helm values for ELK/Prometheus once the target cluster shape is chosen.
3. Add an appointment slot availability API so the UI can hide booked slots from the backend source of truth instead of relying on local UI state.
