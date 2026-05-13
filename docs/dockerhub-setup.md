# Docker Hub Publishing Setup

The final project rubric explicitly asks for generated Docker images to be pushed to Docker Hub. The Jenkins pipeline already builds and pushes images; this setup tells Jenkins where to push them.

## Manual Steps

1. Open Docker Hub: `https://hub.docker.com/`
2. Sign in or create a Docker Hub account.
3. Create an access token:
   - Account Settings -> Personal access tokens -> Generate new token.
   - Name: `swasthya-jenkins`.
   - Permission: Read, Write, Delete is acceptable for project demo; Read, Write is enough if available.
   - Copy the token once.
4. In Jenkins, create Docker Hub credentials:
   - Manage Jenkins -> Credentials -> System -> Global credentials -> Add Credentials.
   - Kind: Username with password.
   - Username: your Docker Hub username.
   - Password: the Docker Hub access token.
   - ID: `swasthya-dockerhub`.
5. If your Docker Hub username is not `adityapareek01`, change the Jenkins build parameter:
   - `IMAGE_REPOSITORY_PREFIX=docker.io/<your-dockerhub-username>`

You do not need to manually create one Docker Hub repository per service. Docker Hub will create repositories on first push if your account/namespace allows it. If it does not, create these repositories manually:

- `swasthya-setu-ai-service`
- `swasthya-setu-backend`
- `swasthya-setu-auth-service`
- `swasthya-setu-hospital-service`
- `swasthya-setu-appointment-service`
- `swasthya-setu-patient-service`
- `swasthya-setu-notification-service`
- `swasthya-setu-api-gateway`
- `swasthya-setu-patient-frontend`
- `swasthya-setu-doctor-frontend`

## Jenkins Parameters

For the Docker Hub publish demo run:

- `RUN_DOCKER_BUILD=false`
- `PUBLISH_IMAGES=true`
- `IMAGE_REPOSITORY_PREFIX=docker.io/<your-dockerhub-username>`
- `IMAGE_TAG=phase-dockerhub-1` or leave empty for Git SHA
- `DOCKER_REGISTRY_URL=docker.io`
- `DOCKER_REGISTRY_CREDENTIALS_ID=swasthya-dockerhub`
- `RUN_LOCAL_DEPLOY=false`
- `RUN_REMOTE_DEPLOY=false`
- `RUN_ANSIBLE_DEPLOY=false`
- `RUN_MINIKUBE_DEPLOY=false`

## Local Equivalent

```bash
docker login docker.io
IMAGE_REPOSITORY_PREFIX=docker.io/<your-dockerhub-username> \
IMAGE_TAG=phase-dockerhub-1 \
sh scripts/ci/publish-images.sh
```

Use Jenkins for the final demo because it proves the automated CI/CD path.
