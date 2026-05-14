pipeline {
    agent any

    environment {
        DOCKER_CREDENTIALS_ID = 'docker-hub-credentials'
        DOCKER_REGISTRY = 'docker.io'
        DOCKER_USERNAME = 'aditya01237'
        SERVICES = 'eureka-server api-gateway auth-service patient-service provider-service appointment-service qr-service swasthya-frontend doctor-frontend'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build Java Projects') {
            steps {
                script {
                    def javaServices = ['eureka-server', 'api-gateway', 'auth-service', 'patient-service', 'provider-service', 'appointment-service', 'qr-service']
                    for (service in javaServices) {
                        dir(service) {
                            sh './mvnw clean package -DskipTests'
                        }
                    }
                }
            }
        }

        stage('Build & Push Docker Images') {
            steps {
                script {
                    withCredentials([usernamePassword(credentialsId: env.DOCKER_CREDENTIALS_ID, usernameVariable: 'USER', passwordVariable: 'PASS')]) {
                        sh "echo \$PASS | docker login -u \$USER --password-stdin"
                        def servicesList = env.SERVICES.split(' ')
                        for (service in servicesList) {
                            dir(service) {
                                def imageName = "${env.DOCKER_USERNAME}/${service}:latest"
                                sh "docker build -t ${imageName} ."
                                sh "docker push ${imageName}"
                            }
                        }
                    }
                }
            }
        }

        stage('Deploy to Kubernetes (Minikube)') {
            steps {
                script {
                    // Apply infra and configs first
                    sh 'kubectl apply -f k8s/configmap.yaml'
                    sh 'kubectl apply -f k8s/infra.yaml'
                    sh 'kubectl apply -f k8s/elk.yaml'
                    
                    // Apply microservices
                    def servicesList = env.SERVICES.split(' ')
                    for (service in servicesList) {
                        sh "kubectl apply -f k8s/${service}.yaml"
                    }
                    
                    // Apply ingress
                    sh 'kubectl apply -f k8s/ingress.yaml'
                }
            }
        }
    }

    post {
        always {
            echo "Pipeline finished."
            sh "docker logout"
        }
        success {
            echo "SwasthyaSetu deployed successfully to Minikube!"
        }
        failure {
            echo "Pipeline failed."
        }
    }
}
