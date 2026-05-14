# Vault Secret Flow in SwasthyaSetu

## Why Vault is Used

SwasthyaSetu uses HashiCorp Vault to demonstrate secure secret handling in the CI/CD pipeline.

Instead of hardcoding sensitive values directly inside Jenkins pipeline commands, the pipeline can seed and read secrets through Vault.

This is useful for secrets such as:

- Docker registry username
- Docker registry password/token
- Deployment credentials
- Environment-specific secret values

## Current Project Flow

For this project, Vault is used mainly in the CI/CD flow.

The flow is:

```text
Jenkins Credentials
        ↓
Jenkins Pipeline
        ↓
Vault KV Secret Store
        ↓
Pipeline reads secrets from Vault
        ↓
Docker login / Docker image publish / deployment scripts
```

## Jenkins to Vault Flow

In Jenkins, sensitive values are stored using Jenkins Credentials.

During the pipeline:

1. Jenkins reads Docker registry credentials from Jenkins Credentials.
2. The pipeline runs the Vault CI script.
3. Vault is started in development mode for local/demo use.
4. Registry credentials are written into Vault KV storage.
5. Later pipeline scripts read those secrets from Vault.
6. Docker login and image publishing use the Vault-managed credentials.

## Development Mode Note

The file `docker-compose.vault.yml` uses Vault in development mode.

This is acceptable for local demonstration because it is simple to run and easy to explain during evaluation.

However, Vault development mode is not suitable for production because:

- The root token is fixed for demo.
- Data is not securely persisted like production Vault.
- Access policies are simplified.
- It is mainly for learning and local CI/CD demonstration.

## Kubernetes Secrets Note

The Kubernetes file `k8s/app-secrets.yaml` contains development placeholder secrets.

Examples include:

```text
postgres-user
postgres-password
rabbitmq-user
rabbitmq-password
jwt-secret
```

These are used only for local Minikube testing.

In a production setup, these secrets should not be committed directly into Git.

## Production Improvement

In production, the better approach would be:

```text
Vault / External Secret Manager
        ↓
External Secrets Operator / Vault Agent
        ↓
Kubernetes Secret generated dynamically
        ↓
Pods consume secrets using env variables or mounted secret files
```

This means Kubernetes secrets would be generated from Vault instead of being manually written in YAML.

## Demo Explanation

During evaluation, we can explain it like this:

```text
For local Minikube, we use placeholder Kubernetes secrets to keep the demo simple and reproducible.
For CI/CD, Jenkins demonstrates Vault-based secret handling for registry credentials.
In production, Kubernetes secrets should be generated from Vault or an external secret manager instead of being committed to Git.
```

## Summary

SwasthyaSetu currently demonstrates Vault at the CI/CD level.

The project uses:

- Jenkins Credentials as the initial secure source
- Vault KV storage for secret flow demonstration
- Vault-based Docker registry login flow
- Local Kubernetes placeholder secrets for Minikube testing

This gives a clear DevOps security story while keeping the project runnable in a local academic environment.
