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
    }

    environment {
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
                expression { params.RUN_DOCKER_BUILD }
            }
            steps {
                sh 'docker compose build'
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
    }

    post {
        always {
            junit allowEmptyResults: true, testResults: 'services/*/target/surefire-reports/*.xml'
            archiveArtifacts allowEmptyArchive: true, artifacts: 'services/*/target/*.jar,swasthya-frontend/dist/**,doctor-frontend/dist/**'
        }
    }
}
