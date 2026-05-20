# ELK Observability Strategy in SwasthyaSetu

## Why ELK is Used

SwasthyaSetu uses the ELK stack to demonstrate centralized logging and observability.

ELK means:

- Elasticsearch: stores and indexes logs
- Logstash: receives and processes logs
- Kibana: visualizes logs using dashboards/search

This helps the DevOps team monitor application behavior, debug failures, and verify service activity from one place.

## Current ELK Implementation

In this project, ELK is implemented using the Docker Compose observability overlay.

The main files are:

```text
docker-compose.observability.yml
docker/logstash/pipeline/logstash.conf
```

The observability overlay adds:

```text
Elasticsearch
Logstash
Kibana
```

Application containers send logs using the GELF logging driver.

Logstash receives those GELF logs and forwards them to Elasticsearch.

Kibana is used to search and visualize the logs.

## Current Log Flow

```text
SwasthyaSetu Microservices
        ↓
Docker GELF logging driver
        ↓
Logstash
        ↓
Elasticsearch index
        ↓
Kibana dashboard/search
```

The Elasticsearch index pattern used is:

```text
swasthya-setu-logs-*
```

## Services Covered in Compose ELK

The Docker Compose observability setup covers logs from major services such as:

- API Gateway
- Auth Service
- Patient Service
- Appointment Service
- Hospital Service
- Notification Service
- Backend
- AI Service
- Patient Frontend
- Doctor Frontend

## Kubernetes Observability Note

The current Kubernetes manifests focus on application deployment, service discovery, ingress, and HPA.

Kubernetes pod logs are not currently shipped to ELK using Fluent Bit, Filebeat, or a Logstash DaemonSet.

So, for evaluation, the safe explanation is:

```text
For ELK, we demonstrate centralized logging using the Docker Compose observability overlay.
For Kubernetes, we demonstrate deployment, service discovery, health checks, and HPA separately.
```

## Why This Separation is Acceptable for Demo

This separation keeps the local project stable and easier to demonstrate.

Running all microservices, databases, Redis, RabbitMQ, Kubernetes, and a full ELK stack inside Minikube can overload a local laptop.

Therefore:

- Docker Compose is used to demonstrate ELK observability.
- Kubernetes is used to demonstrate orchestration and autoscaling.
- Jenkins connects the build, test, Docker, registry, and deployment flow.

## Better Production Approach

In a production Kubernetes environment, pod logs should be shipped to ELK using a log collector.

A better Kubernetes ELK flow would be:

```text
Kubernetes Pods
        ↓
Fluent Bit / Filebeat DaemonSet
        ↓
Logstash or Elasticsearch
        ↓
Kibana
```

In this design:

- Fluent Bit or Filebeat runs on every Kubernetes node.
- It reads container logs from the node.
- Logs are enriched with Kubernetes metadata.
- Logs are sent to Logstash or Elasticsearch.
- Kibana is used for dashboards and debugging.

## Future Improvement

A future improvement for this project is to add:

```text
k8s/observability/fluent-bit-daemonset.yaml
```

This would allow Kubernetes pod logs to be sent directly into Elasticsearch or Logstash.

## Demo Explanation

During evaluation, explain it like this:

```text
SwasthyaSetu demonstrates ELK observability through Docker Compose using GELF logs, Logstash, Elasticsearch, and Kibana.
For Kubernetes, the project demonstrates deployment, services, health checks, ingress readiness, and HPA.
In a production Kubernetes setup, we would add Fluent Bit or Filebeat as a DaemonSet to forward pod logs to ELK.
```

## Summary

The project currently demonstrates:

- Centralized logging through Docker Compose
- GELF-based log forwarding
- Logstash processing
- Elasticsearch indexing
- Kibana visualization
- Kubernetes deployment separately

This gives a clear and stable DevOps observability story for local academic evaluation.