pipeline {
    agent any

    parameters {
        // Turn on once the SonarQube server and scanner are configured in Jenkins
        booleanParam(name: 'RUN_SONAR', defaultValue: false, description: 'Run SonarQube analysis')
    }

    environment {
        DOCKER_IMAGE   = 'vijayragaw/dvp-week12-app'   // Docker Hub repo (lowercase)
        CREDENTIALS_ID = 'dockerhub-jenkins-token'     // Jenkins "Username with password" credential
        IMAGE_TAG      = "${env.BUILD_NUMBER}"

        // SonarQube settings (only used when RUN_SONAR is checked)
        SONAR_SERVER   = 'SonarQube'                   // Manage Jenkins > System > SonarQube servers > Name
        SONAR_SCANNER  = 'SonarScanner'                // Manage Jenkins > Tools > SonarQube Scanner > Name
        SONAR_PROJECT  = 'DVP-Week12-SonarQube-Docker'
    }

    stages {

        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Check Docker') {
            steps {
                bat 'docker version'
            }
        }

        stage('SonarQube Analysis') {
            when { expression { params.RUN_SONAR } }
            steps {
                script {
                    def scannerHome = tool env.SONAR_SCANNER
                    withSonarQubeEnv(env.SONAR_SERVER) {
                        bat "\"${scannerHome}\\bin\\sonar-scanner.bat\" -Dsonar.projectKey=%SONAR_PROJECT% -Dsonar.projectName=%SONAR_PROJECT% -Dsonar.sources=."
                    }
                }
            }
        }

        stage('Build Image') {
            steps {
                bat 'docker build -t %DOCKER_IMAGE%:%IMAGE_TAG% -t %DOCKER_IMAGE%:latest .'
            }
        }

        stage('Push to Docker Hub') {
            steps {
                withCredentials([usernamePassword(credentialsId: env.CREDENTIALS_ID,
                                                  usernameVariable: 'DOCKERHUB_USERNAME',
                                                  passwordVariable: 'DOCKERHUB_PASSWORD')]) {
                    // No space before the | or it becomes part of the password
                    bat '''
                        echo %DOCKERHUB_PASSWORD%| docker login --username %DOCKERHUB_USERNAME% --password-stdin || exit /b 1
                        docker push %DOCKER_IMAGE%:%IMAGE_TAG% || exit /b 1
                        docker push %DOCKER_IMAGE%:latest || exit /b 1
                    '''
                }
            }
        }
    }

    post {
        always {
            bat 'docker logout || exit 0'
        }
        success {
            echo "Pushed ${env.DOCKER_IMAGE}:${env.IMAGE_TAG} and :latest to Docker Hub."
        }
        failure {
            echo 'Pipeline failed. Check the stage logs above.'
        }
    }
}