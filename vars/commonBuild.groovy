def call(Map config = [:]) {
    pipeline {
        agent any

        environment {
            IMAGE_NAME = config.image_name ?: 'skc3766/python_image'
            TAG = config.tag ?: 'latest'
            DOCKER_USER = credentials(config.docker_credentials_id).username
            DOCKER_PASS = credentials(config.docker_credentials_id).password
            GIT_URL = config.git_url ?: 'https://github.com/wolf3766/java_application'
            GIT_BRANCH = config.git_branch ?: 'master'
        }

        stages {
            stage('Checkout') {
                steps {
                    git url: "${env.GIT_URL}", branch: "${env.GIT_BRANCH}"
                }
            }

            stage('Build and Test') {
                steps {
                    sh 'mvn clean install'
                }
            }

            stage('Build Docker Image') {
                steps {
                    sh "docker build -t ${env.IMAGE_NAME}:${env.TAG} ."
                }
            }

            stage('Login & Push Docker Image') {
                steps {
                    withCredentials([usernamePassword(
                        credentialsId: config.docker_credentials_id,
                        usernameVariable: 'DOCKER_USER',
                        passwordVariable: 'DOCKER_PASS'
                    )]) {
     			    sh "docker login -u $DOCKER_USER -p $DOCKER_PASS"
                            sh "docker push ${env.IMAGE_NAME}:$TAG"
                    }
                }
            }

            stage('Deploy to Kubernetes') {
                steps {
                       sh "kubectl delete deployment --all"
                       sh "kubectl create deployment hello1springboot --image=$IMAGE_NAME:$TAG --dry-run=client -o yaml > deploy.yaml"
                       sh "kubectl apply -f deploy.yaml"
                }
            }
        }

        post {
            success {
                echo "✅ Deployment succeeded!"
            }
            failure {
                echo "❌ Deployment failed."
            }
        }
    }
}
