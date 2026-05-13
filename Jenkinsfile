pipeline {
    agent any

    options {
        disableConcurrentBuilds()
        buildDiscarder(logRotator(numToKeepStr: '20'))
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
            defaultValue: false,
            description: 'Build and push app images to the configured Docker registry.'
        )
        string(
            name: 'IMAGE_REPOSITORY_PREFIX',
            defaultValue: 'ghcr.io/aditya01237/swasthya-setu',
            description: 'Registry/repository prefix, for example ghcr.io/user/project or docker.io/user.'
        )
        string(
            name: 'IMAGE_TAG',
            defaultValue: '',
            description: 'Image tag to publish. Leave empty to use the Git commit SHA.'
        )
        string(
            name: 'DOCKER_REGISTRY_URL',
            defaultValue: 'ghcr.io',
            description: 'Registry host used for docker login, for example ghcr.io or docker.io.'
        )
        string(
            name: 'DOCKER_REGISTRY_CREDENTIALS_ID',
            defaultValue: '',
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
    }

    environment {
        PATH = "/opt/homebrew/bin:/usr/local/bin:/usr/bin:/bin:/usr/sbin:/sbin:$PATH"
        COMPOSE_PROJECT_NAME = 'swasthya-setu-ci'
        COMPOSE_DOCKER_CLI_BUILD = '1'
        DOCKER_BUILDKIT = '1'
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
    }

    post {
        always {
            junit allowEmptyResults: true, testResults: 'services/*/target/surefire-reports/*.xml'
            archiveArtifacts allowEmptyArchive: true, artifacts: 'services/*/target/*.jar,swasthya-frontend/dist/**,doctor-frontend/dist/**'
        }
    }
}
