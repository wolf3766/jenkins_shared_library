def call(Map config = [:]) {



  pipeline {

    agent any



    environment {

      BRANCH_NAME = "${config.branch ?: 'main'}"

      REPO_URL = "${config.URL}"

      IMAGE_NAME = "${config.image_name}"

    }



    stages {



      stage('Checkout') {

        steps {

          echo "Checking out branch: ${env.BRANCH_NAME} from ${env.REPO_URL}"

          git branch: "${env.BRANCH_NAME}", url: "${env.REPO_URL}"

        }

      }



      stage('Unit Tests') {

        steps {

          echo "Running unit test cases..."

          // Add your test commands here, e.g., `sh './gradlew test'`

        }

      }



      stage('Build and Push Docker Image') {

        steps {

          echo "Building and pushing Docker image: ${env.IMAGE_NAME}"



          withCredentials([usernamePassword(

            credentialsId: 'docker',

            usernameVariable: 'DOCKER_USER',

            passwordVariable: 'DOCKER_PASS'

          )]) {

            sh """

              echo "$DOCKER_PASS" | docker login -u "$DOCKER_USER" --password-stdin

              docker build -t ${env.IMAGE_NAME} .

              docker push ${env.IMAGE_NAME}

              docker logout

            """

          }

        }

      }



      stage('Deploy to Kubernetes') {

        steps {

          echo "Creating deployments using deployment.yaml"

          sh "kubectl apply -f deployment.yaml"

        }

      }



    }



    post {

      success {

        echo "Pipeline completed successfully!"

      }

      failure {

        echo "Pipeline failed."

      }

    }

  }

}

