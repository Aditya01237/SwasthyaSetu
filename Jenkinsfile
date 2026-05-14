pipeline {
    agent any

    options {
        disableConcurrentBuilds()
        buildDiscarder(logRotator(numToKeepStr: '20'))
        timeout(time: 180, unit: 'MINUTES')
    }

    triggers {
        githubPush()
    }

    parameters {
        booleanParam(
            name: 'RUN_DOCKER_BUILD',
            defaultValue: false,
            description: 'Compatibility flag. Docker image build stage currently runs in the main CI path.'
        )
        booleanParam(
            name: 'RUN_SERVICE_DB_SYNC',
            defaultValue: false,
            description: 'With Ansible deploy: use docker-compose.service-dbs.yml and run the DB sync job.'
        )
        booleanParam(
            name: 'PUBLISH_IMAGES',
            defaultValue: true,
            description: 'Compatibility flag. Publish Docker Images stage currently runs in the main CI path.'
        )
        booleanParam(
            name: 'RUN_MINIKUBE_DEPLOY',
            defaultValue: false,
            description: 'Run the local Minikube deployment stage.'
        )
        booleanParam(
            name: 'RUN_K8S_REGISTRY_DEPLOY',
            defaultValue: false,
            description: 'Deploy Kubernetes manifests using images pulled from the configured registry.'
        )
        booleanParam(
            name: 'RUN_ANSIBLE_DEPLOY',
            defaultValue: false,
            description: 'Run the Ansible remote deployment stage.'
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
            name: 'REMOTE_DEPLOY_PATH',
            defaultValue: 'swasthya-setu',
            description: 'Target directory on the host for Ansible deploy-compose.'
        )
        string(
            name: 'REMOTE_SSH_CREDENTIALS_ID',
            defaultValue: '',
            description: 'Jenkins SSH private key credential ID for remote or Ansible deployment.'
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
            description: 'Run ELK observability verification stage.'
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

        VAULT_PORT = '18200'
        VAULT_DEV_ROOT_TOKEN = 'swasthya-root-token'
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
                            sh 'mvn -B -q test'
                        }
                    }
                }

                stage('auth-service') {
                    steps {
                        dir('services/auth-service') {
                            sh 'mvn -B -q test'
                        }
                    }
                }

                stage('hospital-service') {
                    steps {
                        dir('services/hospital-service') {
                            sh 'mvn -B -q test'
                        }
                    }
                }

                stage('appointment-service') {
                    steps {
                        dir('services/appointment-service') {
                            sh 'mvn -B -q test'
                        }
                    }
                }

                stage('patient-service') {
                    steps {
                        dir('services/patient-service') {
                            sh 'mvn -B -q test'
                        }
                    }
                }

                stage('notification-service') {
                    steps {
                        dir('services/notification-service') {
                            sh 'mvn -B -q test'
                        }
                    }
                }

                stage('backend') {
                    steps {
                        dir('services/backend') {
                            sh 'mvn -B -q test'
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
                            retry(2) {
                                sh 'npm ci'
                                sh 'npm run build'
                            }
                        }
                    }
                }

                stage('doctor-frontend') {
                    steps {
                        dir('doctor-frontend') {
                            retry(2) {
                                sh 'npm ci'
                                sh 'npm run build'
                            }
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

        stage('HashiCorp Vault') {
            steps {
                script {
                    if (!params.DOCKER_REGISTRY_CREDENTIALS_ID?.trim()) {
                        error 'DOCKER_REGISTRY_CREDENTIALS_ID is required to seed Vault registry secrets before publish.'
                    }

                    if (!params.DOCKER_REGISTRY_URL?.trim()) {
                        error 'DOCKER_REGISTRY_URL is required for Vault registry seeding.'
                    }

                    withCredentials([usernamePassword(
                        credentialsId: params.DOCKER_REGISTRY_CREDENTIALS_ID.trim(),
                        usernameVariable: 'REGISTRY_USERNAME',
                        passwordVariable: 'REGISTRY_PASSWORD'
                    )]) {
                        withEnv([
                            "DOCKER_REGISTRY_URL=${params.DOCKER_REGISTRY_URL.trim()}"
                        ]) {
                            sh 'sh scripts/ci/vault-ci.sh'
                        }
                    }
                }
            }
        }

        stage('Build Docker Images') {
            steps {
                sh 'docker compose build'
            }
        }

        stage('Publish Docker Images') {
            steps {
                script {
                    if (!params.DOCKER_REGISTRY_CREDENTIALS_ID?.trim()) {
                        error 'DOCKER_REGISTRY_CREDENTIALS_ID is required for Publish Docker Images.'
                    }

                    if (!params.IMAGE_REPOSITORY_PREFIX?.trim()) {
                        error 'IMAGE_REPOSITORY_PREFIX is required for Publish Docker Images.'
                    }

                    if (!params.DOCKER_REGISTRY_URL?.trim()) {
                        error 'DOCKER_REGISTRY_URL is required for Publish Docker Images.'
                    }

                    def gitSha = env.GIT_COMMIT ?: sh(script: 'git rev-parse HEAD', returnStdout: true).trim()
                    def resolvedImageTag = params.IMAGE_TAG?.trim() ? params.IMAGE_TAG.trim() : gitSha.take(12)

                    withEnv([
                        "IMAGE_REPOSITORY_PREFIX=${params.IMAGE_REPOSITORY_PREFIX.trim()}",
                        "IMAGE_TAG=${resolvedImageTag}",
                        "DOCKER_REGISTRY_URL=${params.DOCKER_REGISTRY_URL.trim()}"
                    ]) {
                        sh 'sh scripts/ci/vault-registry-docker-login.sh'
                        sh 'sh scripts/ci/publish-images.sh'
                    }
                }
            }
        }

        stage('Deploy Minikube') {
            when {
                expression { return params.RUN_MINIKUBE_DEPLOY }
            }
            steps {
                catchError(buildResult: 'SUCCESS', stageResult: 'UNSTABLE') {
                    sh 'sh scripts/ci/deploy-minikube.sh'
                }
            }
        }

        stage('Deploy Kubernetes Registry Images') {
            when {
                expression { return params.RUN_K8S_REGISTRY_DEPLOY }
            }
            steps {
                script {
                    def gitSha = env.GIT_COMMIT ?: sh(script: 'git rev-parse HEAD', returnStdout: true).trim()
                    def resolvedImageTag = params.IMAGE_TAG?.trim() ? params.IMAGE_TAG.trim() : gitSha.take(12)

                    catchError(buildResult: 'SUCCESS', stageResult: 'UNSTABLE') {
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
        }

        stage('Deploy With Ansible') {
            when {
                expression { return params.RUN_ANSIBLE_DEPLOY }
            }
            steps {
                script {
                    catchError(buildResult: 'SUCCESS', stageResult: 'UNSTABLE') {
                        if (!params.REMOTE_SSH_CREDENTIALS_ID?.trim()) {
                            error 'REMOTE_SSH_CREDENTIALS_ID is not set. Configure it before running Ansible deploy.'
                        }

                        if (!params.ANSIBLE_INVENTORY_PATH?.trim() && !params.ANSIBLE_INVENTORY_FILE_CREDENTIALS_ID?.trim()) {
                            error 'ANSIBLE_INVENTORY_PATH or ANSIBLE_INVENTORY_FILE_CREDENTIALS_ID is required for Ansible deploy.'
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
        }

        stage('ELK observability verification') {
            when {
                expression { return params.RUN_ELK_VERIFICATION }
            }
            steps {
                catchError(buildResult: 'SUCCESS', stageResult: 'UNSTABLE') {
                    sh 'sh scripts/ci/check-elk-observability.sh'
                }
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