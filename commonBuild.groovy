def call(Map config = [:]) {
    pipeline {
        agent any
        environment {
            BRANCH_NAME = "${config.branch}"
            URL = "${config.URL}"
            Image_Name = "${config.image_name}"
        }
        stages {
            stage('checkout') {
                steps {
                    echo "checking out"
                    git branch $ {
                        BRANCH_NAME
                    } url: $ {
                        URL
                    }
                }
            }
            stage('Unit tests') {
                steps {
                    echo "running unit test cases"
                }
            }
            stage('Build the image and push image') {
                steps {
                    echo "build the image"
                    withCredentials([usernamePassword(
                        credentialsId: 'docker',
                        usernameVariable: 'docker_user',
                        passwordVariable: 'docker_pass'
                    )]) {
                        sh "docker login -u $docker_user -p $docker_pass"
                        sh "docker build -t ${config.image_name}"
                        sh "docker push ${config.image_name}"
                    }
                }
            }
            stage('deploy') {
                steps {
                    echo "creating deployments"
                    sh "kubectl apply -f deployment.yaml"
                }
            }
        }
    }
}
