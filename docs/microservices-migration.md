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

- Externalized backend DB, mail, JWT, CORS, and AI-service URLs.
- Added Spring Actuator health endpoint for Docker/Kubernetes probes.
- Added `/health` to the FastAPI AI service.
- Added Dockerfiles for backend, AI service, and both frontends.
- Added Docker Compose with PostgreSQL, Redis, RabbitMQ, AI service, current backend, and both frontends.
- Created local logical databases for the future service split: `auth_db`, `patient_db`, `appointment_db`, and `hospital_db`.

## Local Run

Create a local env file from the example and fill secrets:

```bash
cp .env.example .env
docker compose up --build
```

Useful URLs:

- Patient frontend: `http://localhost:5173/patient/`
- Doctor frontend: `http://localhost:5174/doctor/`
- Backend health: `http://localhost:8080/actuator/health`
- AI health: `http://localhost:8000/health`
- RabbitMQ UI: `http://localhost:15672`

## Next Extraction Order

1. Create `api-gateway` and keep frontend traffic on `/api`.
2. Extract `auth-service` on port `8081`.
3. Extract `hospital-service` on port `8084`.
4. Extract `appointment-service` on port `8083` and add Redis slot locks.
5. Extract `patient-service` on port `8082`.
6. Add `notification-service` on port `8086` as RabbitMQ consumers.
