
pipeline {
    agent any

    parameters {
        booleanParam(
            name: 'RUN_SONAR',
            defaultValue: true,
            description: 'Run SonarQube analysis and Quality Gate'
        )
    }

    environment {
        DOCKER_IMAGE   = 'vijayragaw/dvp-week12-app'
        CREDENTIALS_ID = 'dockerhub-jenkins-token'
        IMAGE_TAG      = "${BUILD_NUMBER}"

        SONAR_SERVER   = 'SonarQube'
        SONAR_PROJECT  = 'DVP-Week12'

        CONTAINER_NAME = 'dvp-week12-app'
        APP_PORT       = '8081'
    }

    stages {

        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Check Tools') {
            steps {
                bat 'java -version'
                bat 'mvn -version'
                bat 'docker version'
            }
        }

        stage('Build and Test') {
            steps {
                bat 'mvn -B clean verify'
            }
            post {
                always {
                    junit(
                        testResults: '**/target/surefire-reports/*.xml',
                        allowEmptyResults: true
                    )
                }
            }
        }

        stage('SonarQube Analysis') {
            when {
                expression { params.RUN_SONAR }
            }
            steps {
                withSonarQubeEnv("${SONAR_SERVER}") {
                    bat '''
                        mvn -B org.sonarsource.scanner.maven:sonar-maven-plugin:5.1.0.4751:sonar ^
                        -Dsonar.projectKey=DVP-Week12 ^
                        -Dsonar.projectName="DVP Week 12"
                    '''
                }
            }
        }

        stage('SonarQube Quality Gate') {
            when {
                expression { params.RUN_SONAR }
            }
            steps {
                timeout(time: 10, unit: 'MINUTES') {
                    script {
                        def qualityGate = waitForQualityGate()
                        echo "SonarQube Quality Gate: ${qualityGate.status}"

                        if (qualityGate.status != 'OK') {
                            error "Quality Gate failed: ${qualityGate.status}"
                        }
                    }
                }
            }
        }

        stage('Build Docker Image') {
            steps {
                bat '''
                    docker build ^
                    -t %DOCKER_IMAGE%:%IMAGE_TAG% ^
                    -t %DOCKER_IMAGE%:latest .
                '''
            }
        }

        stage('Push to Docker Hub') {
            steps {
                withCredentials([
                    usernamePassword(
                        credentialsId: 'dockerhub-jenkins-token',
                        usernameVariable: 'DOCKERHUB_USERNAME',
                        passwordVariable: 'DOCKERHUB_PASSWORD'
                    )
                ]) {
                    bat '''
                        echo %DOCKERHUB_PASSWORD%| docker login --username %DOCKERHUB_USERNAME% --password-stdin || exit /b 1

                        docker push %DOCKER_IMAGE%:%IMAGE_TAG% || exit /b 1

                        docker push %DOCKER_IMAGE%:latest || exit /b 1
                    '''
                }
            }
        }

        stage('Deploy Locally') {
            steps {
                bat '''
                    docker rm -f %CONTAINER_NAME% 2>NUL
                    docker run -d ^
                    --name %CONTAINER_NAME% ^
                    --restart unless-stopped ^
                    -p %APP_PORT%:8080 ^
                    %DOCKER_IMAGE%:%IMAGE_TAG%

                    if errorlevel 1 exit /b 1
                '''
            }
        }

        stage('Verify Deployment') {
            steps {
                powershell '''
                    $ErrorActionPreference = "Stop"

                    Start-Sleep -Seconds 5

                    $response = Invoke-WebRequest `
                        -Uri "http://localhost:8081/health" `
                        -UseBasicParsing

                    Write-Host "Application HTTP Status: $($response.StatusCode)"
                    Write-Host "Application Response: $($response.Content)"

                    if ($response.StatusCode -ne 200) {
                        throw "Application health check failed."
                    }
                '''
            }
        }
    }

    post {
        always {
            bat 'docker logout || exit 0'
        }

        success {
            echo "Pipeline completed successfully!"
            echo "Docker Image: ${env.DOCKER_IMAGE}:${env.IMAGE_TAG}"
            echo "Latest Image: ${env.DOCKER_IMAGE}:latest"
            echo "Application URL: http://localhost:8081/health"
        }

        failure {
            echo 'Pipeline failed. Check the failed stage in Console Output.'
        }
    }
}
