# SwasthyaSetu Kubernetes Manifests

Local Minikube setup for the extracted services.

## What This Creates

- `api-gateway`, Spring services, AI service, and both frontends as Kubernetes Deployments.
- ClusterIP Services for service-to-service networking.
- PostgreSQL as a StatefulSet with a PVC for persistent database data.
- Redis, RabbitMQ, and Mailpit for local cache/events/email testing.
- NGINX Ingress routing for `/api`, `/patient`, and `/doctor`.

## Run Locally

Recommended scripted flow:

```bash
sh scripts/ci/deploy-minikube.sh
```

That script starts Minikube if needed, enables the ingress addon, builds app images inside Minikube's Docker daemon, applies `k8s/`, waits for rollouts, and runs service health checks through temporary port-forwards.

To deploy images that Jenkins already pushed to Docker Hub:

```bash
IMAGE_REPOSITORY_PREFIX=docker.io/adityapareek01 \
IMAGE_TAG=your-image-tag \
sh scripts/ci/deploy-k8s-registry.sh
```

This keeps the base manifests local-friendly while rendering registry image names such as `docker.io/adityapareek01/swasthya-setu-auth-service:your-image-tag` at deploy time.

Manual equivalent:

```bash
minikube start
minikube addons enable ingress
eval $(minikube docker-env)
docker compose build
kubectl apply -k k8s
sh scripts/ci/health-check-k8s.sh
```

Map the Minikube IP:

```bash
echo "$(minikube ip) swasthya.local" | sudo tee -a /etc/hosts
```

Useful URLs:

- Patient frontend: `http://swasthya.local/patient/`
- Doctor frontend: `http://swasthya.local/doctor/`
- API gateway health: `http://swasthya.local/api/actuator/health`

The health-check script also opens temporary local ports:

- Patient frontend: `http://localhost:25173/patient/`
- Doctor frontend: `http://localhost:25174/doctor/`
- API gateway: `http://localhost:28080/actuator/health`
- AI service: `http://localhost:28000/health`

For local email inspection in **Mailpit** (only if SMTP in `app-config.yaml` points at `mailpit`):

```bash
kubectl -n swasthya-setu port-forward svc/mailpit 8025:8025
```

Then open `http://localhost:8025`. With the default **Gmail** SMTP settings, OTP mail does not pass through Mailpit.

RabbitMQ management UI:

```bash
kubectl -n swasthya-setu port-forward svc/rabbitmq 15672:15672
```

Then open `http://localhost:15672`.

## Gmail OTP on Minikube

The `swasthya-config` ConfigMap uses **Gmail SMTP** (`smtp.gmail.com:587` with STARTTLS). `backend` and `notification-service` read `SPRING_MAIL_USERNAME` / `SPRING_MAIL_PASSWORD` from the `swasthya-secrets` Secret (`spring-mail-username`, `spring-mail-password`).

1. Edit `k8s/app-secrets.yaml`: set `spring-mail-username` to your Gmail address and `spring-mail-password` to a [Gmail App Password](https://support.google.com/accounts/answer/185833) (2FA must be on).
2. Apply and restart the mail-sending workloads:

```bash
kubectl apply -f k8s/app-secrets.yaml
kubectl -n swasthya-setu rollout restart deployment/notification-service deployment/backend
```

OTP emails then go to the **patient’s registered email** (the address used at login), not necessarily the same mailbox as `spring-mail-username`.

To use **Mailpit** in the cluster instead, set in `app-config.yaml` again: `SPRING_MAIL_HOST: mailpit`, `SPRING_MAIL_PORT: "1025"`, `SPRING_MAIL_PROPERTIES_MAIL_SMTP_AUTH: "false"`, `SPRING_MAIL_PROPERTIES_MAIL_SMTP_STARTTLS_ENABLE: "false"`, apply, and restart `notification-service` and `backend`.

## Secrets

`app-secrets.yaml` contains development placeholders only. Before using this outside local Minikube, replace those values or create the `swasthya-secrets` Secret from your real secret manager.
