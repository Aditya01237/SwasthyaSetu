pipeline {
    agent any

    options {
        disableConcurrentBuilds()
        buildDiscarder(logRotator(numToKeepStr: '20'))
    }

    triggers {
        // Git push webhook (requires GitHub plugin + repository webhook). For GIT SCM polling without hooks,
        // add "Poll SCM" under the job's Build Triggers (per CSE 816: GitHub Hook / polling options).
        githubPush()
    }

    parameters {
        booleanParam(
            name: 'RUN_DOCKER_BUILD',
            defaultValue: true,
            description: 'Build all Docker images with Docker Compose after tests pass.'
        )
        booleanParam(
            name: 'RUN_LOCAL_DEPLOY',
            defaultValue: false,
            description: 'Deploy the stack on the Jenkins Docker host after a successful build.'
        )
        booleanParam(
            name: 'RUN_SERVICE_DB_SYNC',
            defaultValue: false,
            description: 'When deploying locally, use service-owned databases and run the DB sync job.'
        )
        booleanParam(
            name: 'PUBLISH_IMAGES',
            defaultValue: true,
            description: 'Build and push app images to the configured Docker registry.'
        )
        booleanParam(
            name: 'RUN_MINIKUBE_DEPLOY',
            defaultValue: false,
            description: 'Deploy the stack to local Minikube after a successful build.'
        )
        booleanParam(
            name: 'RUN_K8S_REGISTRY_DEPLOY',
            defaultValue: false,
            description: 'Deploy published registry images to the configured Kubernetes context. Enable only when this Jenkins agent has a valid kubectl context (otherwise the stage fails).'
        )
        booleanParam(
            name: 'RUN_ANSIBLE_DEPLOY',
            defaultValue: false,
            description: 'Deploy published images to a remote Docker host with Ansible.'
        )
        booleanParam(
            name: 'ANSIBLE_SETUP_DOCKER',
            defaultValue: true,
            description: 'When using Ansible deploy, install/start Docker on the remote host before deployment.'
        )
        string(
            name: 'IMAGE_REPOSITORY_PREFIX',
            defaultValue: 'docker.io/adityapareek01',
            description: 'Registry namespace prefix, for example docker.io/user.'
        )
        string(
            name: 'IMAGE_TAG',
            defaultValue: '',
            description: 'Image tag to publish. Leave empty to use the Git commit SHA.'
        )
        string(
            name: 'DOCKER_REGISTRY_URL',
            defaultValue: 'docker.io',
            description: 'Registry host used for docker login, for Docker Hub use docker.io.'
        )
        string(
            name: 'DOCKER_REGISTRY_CREDENTIALS_ID',
            defaultValue: 'swasthya-dockerhub',
            description: 'Jenkins username/password credentials ID for the Docker registry.'
        )
        booleanParam(
            name: 'RUN_REMOTE_DEPLOY',
            defaultValue: false,
            description: 'Deploy the pushed images on a remote Docker host over SSH.'
        )
        string(
            name: 'REMOTE_DEPLOY_HOST',
            defaultValue: '',
            description: 'Remote server hostname or IP address.'
        )
        string(
            name: 'REMOTE_DEPLOY_USER',
            defaultValue: 'deploy',
            description: 'Remote SSH user that can run Docker Compose.'
        )
        string(
            name: 'REMOTE_DEPLOY_PATH',
            defaultValue: 'swasthya-setu',
            description: 'Remote deployment directory. Avoid spaces.'
        )
        string(
            name: 'REMOTE_SSH_CREDENTIALS_ID',
            defaultValue: '',
            description: 'Jenkins SSH private key credentials ID for the remote server.'
        )
        string(
            name: 'REMOTE_ENV_FILE_CREDENTIALS_ID',
            defaultValue: '',
            description: 'Optional Jenkins secret file credentials ID for the remote .env file.'
        )
        string(
            name: 'ANSIBLE_INVENTORY_PATH',
            defaultValue: 'ansible/inventory.example.ini',
            description: 'Inventory path for Ansible deploy when no secret inventory file is provided.'
        )
        string(
            name: 'ANSIBLE_INVENTORY_FILE_CREDENTIALS_ID',
            defaultValue: '',
            description: 'Optional Jenkins secret file credentials ID for the Ansible inventory.'
        )
        booleanParam(
            name: 'RUN_ELK_VERIFICATION',
            defaultValue: false,
            description: 'CSE 816 / observability: start Elasticsearch, Logstash, and Kibana (Compose overlay), then run ELK health checks and a GELF smoke test into the swasthya-setu-logs-* index. Requires Docker on the Jenkins agent; can be memory-heavy.'
        )
    }

    environment {
        PATH = "/opt/homebrew/bin:/usr/local/bin:/usr/bin:/bin:/usr/sbin:/sbin:$PATH"
        COMPOSE_PROJECT_NAME = 'swasthya-setu-ci'
        COMPOSE_DOCKER_CLI_BUILD = '1'
        DOCKER_BUILDKIT = '1'
        MINIKUBE_PROFILE = 'minikube'
        K8S_NAMESPACE = 'swasthya-setu'
        POSTGRES_PORT = '15433'
        REDIS_PORT = '16379'
        RABBITMQ_PORT = '15673'
        RABBITMQ_MANAGEMENT_PORT = '15674'
        MAILPIT_SMTP_PORT = '11025'
        MAILPIT_UI_PORT = '18025'
        GATEWAY_PORT = '18080'
        AUTH_SERVICE_PORT = '18081'
        PATIENT_SERVICE_PORT = '18082'
        APPOINTMENT_SERVICE_PORT = '18083'
        HOSPITAL_SERVICE_PORT = '18084'
        NOTIFICATION_SERVICE_PORT = '18086'
        BACKEND_PORT = '18090'
        AI_SERVICE_PORT = '18000'
        PATIENT_FRONTEND_PORT = '15173'
        DOCTOR_FRONTEND_PORT = '15174'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Validate Config') {
            steps {
                sh 'sh scripts/ci/validate-config.sh'
            }
        }

        stage('Test Java Services') {
            parallel {
                stage('api-gateway') {
                    steps {
                        dir('services/api-gateway') {
                            sh 'mvn -q test'
                        }
                    }
                }
                stage('auth-service') {
                    steps {
                        dir('services/auth-service') {
                            sh 'mvn -q test'
                        }
                    }
                }
                stage('hospital-service') {
                    steps {
                        dir('services/hospital-service') {
                            sh 'mvn -q test'
                        }
                    }
                }
                stage('appointment-service') {
                    steps {
                        dir('services/appointment-service') {
                            sh 'mvn -q test'
                        }
                    }
                }
                stage('patient-service') {
                    steps {
                        dir('services/patient-service') {
                            sh 'mvn -q test'
                        }
                    }
                }
                stage('notification-service') {
                    steps {
                        dir('services/notification-service') {
                            sh 'mvn -q test'
                        }
                    }
                }
                stage('backend') {
                    steps {
                        dir('services/backend') {
                            sh 'mvn -q test'
                        }
                    }
                }
            }
        }

        stage('Build Frontends') {
            parallel {
                stage('patient-frontend') {
                    steps {
                        dir('swasthya-frontend') {
                            sh 'npm ci'
                            sh 'npm run build'
                        }
                    }
                }
                stage('doctor-frontend') {
                    steps {
                        dir('doctor-frontend') {
                            sh 'npm ci'
                            sh 'npm run build'
                        }
                    }
                }
            }
        }

        stage('Check AI Service') {
            steps {
                sh 'PYTHONPYCACHEPREFIX=.pycache python3 -m py_compile services/ai-service/app.py'
            }
        }

        stage('Build Docker Images') {
            when {
                expression { params.RUN_DOCKER_BUILD && !params.PUBLISH_IMAGES && !params.RUN_MINIKUBE_DEPLOY && !params.RUN_K8S_REGISTRY_DEPLOY && !params.RUN_ANSIBLE_DEPLOY }
            }
            steps {
                sh 'docker compose build'
            }
        }

        stage('Publish Docker Images') {
            when {
                expression { params.PUBLISH_IMAGES }
            }
            steps {
                script {
                    if (!params.DOCKER_REGISTRY_CREDENTIALS_ID?.trim()) {
                        error 'DOCKER_REGISTRY_CREDENTIALS_ID is required when PUBLISH_IMAGES is true.'
                    }
                    if (!params.IMAGE_REPOSITORY_PREFIX?.trim()) {
                        error 'IMAGE_REPOSITORY_PREFIX is required when PUBLISH_IMAGES is true.'
                    }
                    if (!params.DOCKER_REGISTRY_URL?.trim()) {
                        error 'DOCKER_REGISTRY_URL is required when PUBLISH_IMAGES is true.'
                    }

                    def gitSha = env.GIT_COMMIT ?: sh(script: 'git rev-parse HEAD', returnStdout: true).trim()
                    def resolvedImageTag = params.IMAGE_TAG?.trim() ? params.IMAGE_TAG.trim() : gitSha.take(12)

                    withEnv([
                        "IMAGE_REPOSITORY_PREFIX=${params.IMAGE_REPOSITORY_PREFIX.trim()}",
                        "IMAGE_TAG=${resolvedImageTag}",
                        "DOCKER_REGISTRY_URL=${params.DOCKER_REGISTRY_URL.trim()}"
                    ]) {
                        withCredentials([usernamePassword(
                            credentialsId: params.DOCKER_REGISTRY_CREDENTIALS_ID.trim(),
                            usernameVariable: 'REGISTRY_USERNAME',
                            passwordVariable: 'REGISTRY_PASSWORD'
                        )]) {
                            sh 'echo "$REGISTRY_PASSWORD" | docker login "$DOCKER_REGISTRY_URL" -u "$REGISTRY_USERNAME" --password-stdin'
                        }

                        sh 'sh scripts/ci/publish-images.sh'
                    }
                }
            }
        }

        stage('Deploy Local Compose') {
            when {
                expression { params.RUN_LOCAL_DEPLOY }
            }
            steps {
                sh 'sh scripts/ci/deploy-compose.sh'
            }
        }

        stage('Deploy Minikube') {
            when {
                expression { params.RUN_MINIKUBE_DEPLOY }
            }
            steps {
                sh 'sh scripts/ci/deploy-minikube.sh'
            }
        }

        stage('Deploy Kubernetes Registry Images') {
            when {
                expression { params.RUN_K8S_REGISTRY_DEPLOY }
            }
            steps {
                script {
                    if (!params.PUBLISH_IMAGES) {
                        error 'RUN_K8S_REGISTRY_DEPLOY requires PUBLISH_IMAGES so Kubernetes can pull the images built by this pipeline.'
                    }
                    if (params.RUN_MINIKUBE_DEPLOY) {
                        error 'Choose either RUN_MINIKUBE_DEPLOY or RUN_K8S_REGISTRY_DEPLOY, not both.'
                    }

                    def gitSha = env.GIT_COMMIT ?: sh(script: 'git rev-parse HEAD', returnStdout: true).trim()
                    def resolvedImageTag = params.IMAGE_TAG?.trim() ? params.IMAGE_TAG.trim() : gitSha.take(12)

                    withEnv([
                        "IMAGE_REPOSITORY_PREFIX=${params.IMAGE_REPOSITORY_PREFIX.trim()}",
                        "IMAGE_TAG=${resolvedImageTag}",
                        "K8S_NAMESPACE=${env.K8S_NAMESPACE}"
                    ]) {
                        sh 'sh scripts/ci/deploy-k8s-registry.sh'
                    }
                }
            }
        }

        stage('Deploy Remote Compose') {
            when {
                expression { params.RUN_REMOTE_DEPLOY }
            }
            steps {
                script {
                    if (!params.PUBLISH_IMAGES) {
                        error 'RUN_REMOTE_DEPLOY requires PUBLISH_IMAGES so the remote host can pull immutable images.'
                    }
                    if (!params.REMOTE_DEPLOY_HOST?.trim()) {
                        error 'REMOTE_DEPLOY_HOST is required when RUN_REMOTE_DEPLOY is true.'
                    }
                    if (!params.REMOTE_SSH_CREDENTIALS_ID?.trim()) {
                        error 'REMOTE_SSH_CREDENTIALS_ID is required when RUN_REMOTE_DEPLOY is true.'
                    }
                    if (!params.REMOTE_DEPLOY_USER?.trim()) {
                        error 'REMOTE_DEPLOY_USER is required when RUN_REMOTE_DEPLOY is true.'
                    }
                    if (!params.REMOTE_DEPLOY_PATH?.trim()) {
                        error 'REMOTE_DEPLOY_PATH is required when RUN_REMOTE_DEPLOY is true.'
                    }

                    def gitSha = env.GIT_COMMIT ?: sh(script: 'git rev-parse HEAD', returnStdout: true).trim()
                    def resolvedImageTag = params.IMAGE_TAG?.trim() ? params.IMAGE_TAG.trim() : gitSha.take(12)
                    def remoteEnv = [
                        "IMAGE_REPOSITORY_PREFIX=${params.IMAGE_REPOSITORY_PREFIX.trim()}",
                        "IMAGE_TAG=${resolvedImageTag}",
                        "RUN_SERVICE_DB_SYNC=${params.RUN_SERVICE_DB_SYNC}",
                        "REMOTE_DEPLOY_HOST=${params.REMOTE_DEPLOY_HOST.trim()}",
                        "REMOTE_DEPLOY_USER=${params.REMOTE_DEPLOY_USER.trim()}",
                        "REMOTE_DEPLOY_PATH=${params.REMOTE_DEPLOY_PATH.trim()}"
                    ]

                    sshagent(credentials: [params.REMOTE_SSH_CREDENTIALS_ID.trim()]) {
                        if (params.REMOTE_ENV_FILE_CREDENTIALS_ID?.trim()) {
                            withCredentials([file(credentialsId: params.REMOTE_ENV_FILE_CREDENTIALS_ID.trim(), variable: 'REMOTE_ENV_FILE')]) {
                                withEnv(remoteEnv) {
                                    sh 'sh scripts/ci/deploy-remote-compose.sh'
                                }
                            }
                        } else {
                            withEnv(remoteEnv) {
                                sh 'sh scripts/ci/deploy-remote-compose.sh'
                            }
                        }
                    }
                }
            }
        }

        stage('Deploy With Ansible') {
            when {
                expression { params.RUN_ANSIBLE_DEPLOY }
            }
            steps {
                script {
                    if (!params.PUBLISH_IMAGES) {
                        error 'RUN_ANSIBLE_DEPLOY requires PUBLISH_IMAGES so Ansible can pull immutable images.'
                    }
                    if (params.RUN_REMOTE_DEPLOY) {
                        error 'Choose either RUN_REMOTE_DEPLOY or RUN_ANSIBLE_DEPLOY, not both.'
                    }
                    if (!params.REMOTE_SSH_CREDENTIALS_ID?.trim()) {
                        error 'REMOTE_SSH_CREDENTIALS_ID is required when RUN_ANSIBLE_DEPLOY is true.'
                    }
                    if (!params.ANSIBLE_INVENTORY_PATH?.trim() && !params.ANSIBLE_INVENTORY_FILE_CREDENTIALS_ID?.trim()) {
                        error 'ANSIBLE_INVENTORY_PATH or ANSIBLE_INVENTORY_FILE_CREDENTIALS_ID is required when RUN_ANSIBLE_DEPLOY is true.'
                    }

                    def gitSha = env.GIT_COMMIT ?: sh(script: 'git rev-parse HEAD', returnStdout: true).trim()
                    def resolvedImageTag = params.IMAGE_TAG?.trim() ? params.IMAGE_TAG.trim() : gitSha.take(12)
                    def ansibleEnv = [
                        "IMAGE_REPOSITORY_PREFIX=${params.IMAGE_REPOSITORY_PREFIX.trim()}",
                        "IMAGE_TAG=${resolvedImageTag}",
                        "RUN_SERVICE_DB_SYNC=${params.RUN_SERVICE_DB_SYNC}",
                        "REMOTE_DEPLOY_PATH=${params.REMOTE_DEPLOY_PATH.trim()}",
                        "ANSIBLE_SETUP_DOCKER=${params.ANSIBLE_SETUP_DOCKER}",
                        "DOCKER_REGISTRY_URL=${params.DOCKER_REGISTRY_URL.trim()}"
                    ]

                    sshagent(credentials: [params.REMOTE_SSH_CREDENTIALS_ID.trim()]) {
                        withCredentials([usernamePassword(
                            credentialsId: params.DOCKER_REGISTRY_CREDENTIALS_ID.trim(),
                            usernameVariable: 'REGISTRY_USERNAME',
                            passwordVariable: 'REGISTRY_PASSWORD'
                        )]) {
                            if (params.REMOTE_ENV_FILE_CREDENTIALS_ID?.trim()) {
                                withCredentials([file(credentialsId: params.REMOTE_ENV_FILE_CREDENTIALS_ID.trim(), variable: 'REMOTE_ENV_FILE')]) {
                                    if (params.ANSIBLE_INVENTORY_FILE_CREDENTIALS_ID?.trim()) {
                                        withCredentials([file(credentialsId: params.ANSIBLE_INVENTORY_FILE_CREDENTIALS_ID.trim(), variable: 'ANSIBLE_INVENTORY_FILE')]) {
                                            withEnv(ansibleEnv) {
                                                sh 'sh scripts/ci/deploy-ansible.sh'
                                            }
                                        }
                                    } else {
                                        withEnv(ansibleEnv + ["ANSIBLE_INVENTORY=${params.ANSIBLE_INVENTORY_PATH.trim()}"]) {
                                            sh 'sh scripts/ci/deploy-ansible.sh'
                                        }
                                    }
                                }
                            } else if (params.ANSIBLE_INVENTORY_FILE_CREDENTIALS_ID?.trim()) {
                                withCredentials([file(credentialsId: params.ANSIBLE_INVENTORY_FILE_CREDENTIALS_ID.trim(), variable: 'ANSIBLE_INVENTORY_FILE')]) {
                                    withEnv(ansibleEnv) {
                                        sh 'sh scripts/ci/deploy-ansible.sh'
                                    }
                                }
                            } else {
                                withEnv(ansibleEnv + ["ANSIBLE_INVENTORY=${params.ANSIBLE_INVENTORY_PATH.trim()}"]) {
                                    sh 'sh scripts/ci/deploy-ansible.sh'
                                }
                            }
                        }
                    }
                }
            }
        }

        stage('ELK observability verification') {
            when {
                expression { params.RUN_ELK_VERIFICATION }
            }
            steps {
                sh 'sh scripts/ci/check-elk-observability.sh'
            }
        }
    }

    post {
        always {
            junit allowEmptyResults: true, testResults: 'services/*/target/surefire-reports/*.xml'
            archiveArtifacts allowEmptyArchive: true, artifacts: 'services/*/target/*.jar,swasthya-frontend/dist/**,doctor-frontend/dist/**'
        }
    }
}
