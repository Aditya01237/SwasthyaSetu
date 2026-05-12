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
- Added `api-gateway` on port `8080` as the first routing layer. During transition it routes all `/api/*` traffic to the current monolith on port `8081`.
- Added `auth-service` on port `8081` and routed `/api/auth/**` to it through the gateway. During the transition it reads the existing patient/doctor/hospital tables from `swasthyasetudb` and stores OTP state in `auth_otp_verification`; once patient/hospital services are extracted, this can move fully to `auth_db`.
- Added `hospital-service` on port `8084` for hospital and doctor catalog endpoints. During the transition it uses the existing hospital/doctor tables in `swasthyasetudb`; later this can move to `hospital_db`.
- Added `appointment-service` on port `8083` for appointment booking, doctor schedule, patient appointment details, and QR scanning. It uses Redis slot locks with keys like `slot:{doctorId}:{appointmentTime}` and keeps the existing DB tables during the transition.
- Added `patient-service` on port `8082` for patient registration, profile history, QR audit logs, and prescription uploads. The gateway now forwards validated JWT claims as `X-User-Id` and `X-User-Role` headers; `/api/patient/register` remains public while other patient routes are protected.

## Local Run

Create a local env file from the example and fill secrets:

```bash
cp .env.example .env
docker compose up --build
```

Useful URLs:

- Patient frontend: `http://localhost:5173/patient/`
- Doctor frontend: `http://localhost:5174/doctor/`
- Gateway health: `http://localhost:8080/actuator/health`
- Auth service health: `http://localhost:8081/actuator/health`
- Patient service health: `http://localhost:8082/actuator/health`
- Appointment service health: `http://localhost:8083/actuator/health`
- Hospital service health: `http://localhost:8084/actuator/health`
- Transition backend health: `http://localhost:8090/actuator/health`
- AI health: `http://localhost:8000/health`
- RabbitMQ UI: `http://localhost:15672`

## Next Extraction Order

1. Add `notification-service` on port `8086` as RabbitMQ consumers.
2. Publish RabbitMQ events from auth and appointment flows.
