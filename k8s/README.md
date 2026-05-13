# SwasthyaSetu Kubernetes Manifests

Local Minikube setup for the extracted services.

## What This Creates

- `api-gateway`, Spring services, AI service, and both frontends as Kubernetes Deployments.
- ClusterIP Services for service-to-service networking.
- PostgreSQL as a StatefulSet with a PVC for persistent database data.
- Redis, RabbitMQ, and Mailpit for local cache/events/email testing.
- NGINX Ingress routing for `/api`, `/patient`, and `/doctor`.

## Run Locally

```bash
minikube start
minikube addons enable ingress
eval $(minikube docker-env)
docker compose build
kubectl apply -k k8s
kubectl -n swasthya-setu get pods
```

Map the Minikube IP:

```bash
echo "$(minikube ip) swasthya.local" | sudo tee -a /etc/hosts
```

Useful URLs:

- Patient frontend: `http://swasthya.local/patient/`
- Doctor frontend: `http://swasthya.local/doctor/`
- API gateway health: `http://swasthya.local/api/actuator/health`

For local email inspection:

```bash
kubectl -n swasthya-setu port-forward svc/mailpit 8025:8025
```

Then open `http://localhost:8025`.

RabbitMQ management UI:

```bash
kubectl -n swasthya-setu port-forward svc/rabbitmq 15672:15672
```

Then open `http://localhost:15672`.

## Secrets

`app-secrets.yaml` contains development placeholders only. Before using this outside local Minikube, replace those values or create the `swasthya-secrets` Secret from your real secret manager.
