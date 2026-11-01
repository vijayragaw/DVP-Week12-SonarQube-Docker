
pipeline {
    agent any

    triggers {
        githubPush()
    }

    environment {
        // Replace with your actual Docker Hub username.
        DOCKERHUB_REPO = 'vijayragaw/dvp-week12-app'
        IMAGE_TAG = "${BUILD_NUMBER}"
        CONTAINER_NAME = 'dvp-week12-app'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build and Test') {
            steps {
                bat 'mvn -B clean verify'
            }
            post {
                always {
                    junit allowEmptyResults: true,
                          testResults: 'target/surefire-reports/*.xml'
                }
            }
        }

      stage('SonarQube Analysis') {
    steps {
        withSonarQubeEnv('SonarQube') {
            bat 'mvn -B org.sonarsource.scanner.maven:sonar-maven-plugin:5.1.0.4751:sonar -Dsonar.projectKey=DVP-Week12 -Dsonar.projectName="DVP Week 12"'
        }
    }
}

        stage('Quality Gate') {
            steps {
                timeout(time: 5, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }

        stage('Docker Build') {
            steps {
                bat 'docker build -t %DOCKERHUB_REPO%:%IMAGE_TAG% .'
                bat 'docker tag %DOCKERHUB_REPO%:%IMAGE_TAG% %DOCKERHUB_REPO%:latest'
            }
        }

        stage('Push to DockerHub') {
            steps {
                withCredentials([
                    usernamePassword(
                        credentialsId: 'dockerhub-jenkins-token',
                        usernameVariable: 'DOCKERHUB_USERNAME',
                        passwordVariable: 'DOCKERHUB_PASSWORD'
                    )
                ]) {
             powershell '''
    Write-Host "Docker Hub username: $env:DOCKERHUB_USERNAME"
    Write-Host "Token length: $($env:DOCKERHUB_PASSWORD.Length)"
    Write-Host "Token is empty: $([string]::IsNullOrEmpty($env:DOCKERHUB_PASSWORD))"
'''
                    bat 'docker push %DOCKERHUB_REPO%:%IMAGE_TAG%'
                    bat 'docker push %DOCKERHUB_REPO%:latest'
                    bat 'docker logout'
                }
            }
        }

        stage('Deploy Locally') {
            steps {
                bat 'docker rm -f %CONTAINER_NAME% 2>NUL || exit /b 0'
                bat 'docker run -d --name %CONTAINER_NAME% --restart unless-stopped -p 8081:8080 %DOCKERHUB_REPO%:%IMAGE_TAG%'
            }
        }
    }

    post {
        success {
            echo 'Week 12 CI/CD pipeline completed successfully.'
        }
        failure {
            echo 'Pipeline failed. Review the stage logs above.'
        }
    }
}
