pipeline {
    agent any

    options {
        disableConcurrentBuilds()
        buildDiscarder(logRotator(numToKeepStr: '20'))
        timeout(time: 90, unit: 'MINUTES')
    }

    // CSE 816: enable "GitHub hook trigger for GITScm polling" on the job + GitHub webhook → …/github-webhook/
    triggers {
        githubPush()
    }

    parameters {
        booleanParam(
            name: 'RUN_DOCKER_BUILD',
            defaultValue: false,
            description: 'docker compose build (main compose file only). Skipped when PUBLISH_IMAGES is true (publish script builds images).'
        )
        booleanParam(
            name: 'RUN_LOCAL_DEPLOY',
            defaultValue: true,
            description: 'After publish (or after compose build), deploy on the Jenkins host with scripts/ci/deploy-compose.sh.'
        )
        booleanParam(
            name: 'RUN_SERVICE_DB_SYNC',
            defaultValue: false,
            description: 'With local deploy: use docker-compose.service-dbs.yml and run the DB sync job.'
        )
        booleanParam(
            name: 'PUBLISH_IMAGES',
            defaultValue: true,
            description: 'Build and push images with scripts/ci/publish-images.sh (needs DOCKER_REGISTRY_CREDENTIALS_ID).'
        )
        booleanParam(
            name: 'RUN_MINIKUBE_DEPLOY',
            defaultValue: false,
            description: 'Run scripts/ci/deploy-minikube.sh (needs minikube + kubectl on the agent).'
        )
        booleanParam(
            name: 'RUN_K8S_REGISTRY_DEPLOY',
            defaultValue: false,
            description: 'Run scripts/ci/deploy-k8s-registry.sh (needs kubectl; requires PUBLISH_IMAGES). Mutually exclusive with RUN_MINIKUBE_DEPLOY.'
        )
        booleanParam(
            name: 'RUN_REMOTE_DEPLOY',
            defaultValue: false,
            description: 'SSH deploy with scripts/ci/deploy-remote-compose.sh (requires PUBLISH_IMAGES and SSH credentials).'
        )
        booleanParam(
            name: 'RUN_ANSIBLE_DEPLOY',
            defaultValue: false,
            description: 'Ansible deploy with scripts/ci/deploy-ansible.sh (requires PUBLISH_IMAGES; not with RUN_REMOTE_DEPLOY).'
        )
        booleanParam(
            name: 'ANSIBLE_SETUP_DOCKER',
            defaultValue: true,
            description: 'When using Ansible deploy, install/start Docker on the target first.'
        )
        string(
            name: 'IMAGE_REPOSITORY_PREFIX',
            defaultValue: 'docker.io/adityapareek01',
            description: 'Registry namespace prefix.'
        )
        string(
            name: 'IMAGE_TAG',
            defaultValue: '',
            description: 'Image tag; empty = first 12 chars of Git SHA.'
        )
        string(
            name: 'DOCKER_REGISTRY_URL',
            defaultValue: 'docker.io',
            description: 'Registry host for docker login.'
        )
        string(
            name: 'DOCKER_REGISTRY_CREDENTIALS_ID',
            defaultValue: 'swasthya-dockerhub',
            description: 'Jenkins username/password credential ID for registry login.'
        )
        string(
            name: 'REMOTE_DEPLOY_HOST',
            defaultValue: '',
            description: 'Remote host for RUN_REMOTE_DEPLOY.'
        )
        string(
            name: 'REMOTE_DEPLOY_USER',
            defaultValue: 'deploy',
            description: 'Remote SSH user.'
        )
        string(
            name: 'REMOTE_DEPLOY_PATH',
            defaultValue: 'swasthya-setu',
            description: 'Remote directory for compose deploy.'
        )
        string(
            name: 'REMOTE_SSH_CREDENTIALS_ID',
            defaultValue: '',
            description: 'Jenkins SSH private key credential ID (remote or Ansible).'
        )
        string(
            name: 'REMOTE_ENV_FILE_CREDENTIALS_ID',
            defaultValue: '',
            description: 'Optional secret file credential for remote .env.'
        )
        string(
            name: 'ANSIBLE_INVENTORY_PATH',
            defaultValue: 'ansible/inventory.example.ini',
            description: 'Ansible inventory path when not using a secret inventory file.'
        )
        string(
            name: 'ANSIBLE_INVENTORY_FILE_CREDENTIALS_ID',
            defaultValue: '',
            description: 'Optional secret file credential for Ansible inventory.'
        )
        booleanParam(
            name: 'RUN_ELK_VERIFICATION',
            defaultValue: false,
            description: 'Run scripts/ci/check-elk-observability.sh (heavy; needs Docker + overlay compose).'
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
            steps {
                sh 'sh scripts/ci/test-java-services.sh'
            }
        }

        stage('Build Frontends') {
            steps {
                dir('swasthya-frontend') {
                    retry(2) {
                        sh 'npm ci'
                        sh 'npm run build'
                    }
                }
                dir('doctor-frontend') {
                    retry(2) {
                        sh 'npm ci'
                        sh 'npm run build'
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
                expression { params.RUN_DOCKER_BUILD && !params.PUBLISH_IMAGES }
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
                        error 'RUN_K8S_REGISTRY_DEPLOY requires PUBLISH_IMAGES.'
                    }
                    if (params.RUN_MINIKUBE_DEPLOY) {
                        error 'Use either RUN_MINIKUBE_DEPLOY or RUN_K8S_REGISTRY_DEPLOY, not both.'
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
                        error 'RUN_REMOTE_DEPLOY requires PUBLISH_IMAGES.'
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
                        error 'RUN_ANSIBLE_DEPLOY requires PUBLISH_IMAGES.'
                    }
                    if (params.RUN_REMOTE_DEPLOY) {
                        error 'Use either RUN_REMOTE_DEPLOY or RUN_ANSIBLE_DEPLOY, not both.'
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
