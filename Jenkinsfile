
pipeline {
    agent any

    triggers {
        githubPush()
    }

    environment {
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
                catchError(
                    buildResult: 'UNSTABLE',
                    stageResult: 'FAILURE'
                ) {
                    bat 'mvn -B clean verify'
                }
            }

            post {
                always {
                    junit(
                        allowEmptyResults: true,
                        testResults: 'target/surefire-reports/*.xml'
                    )
                }
            }
        }

        stage('SonarQube Analysis') {
            steps {
                catchError(
                    buildResult: 'UNSTABLE',
                    stageResult: 'FAILURE'
                ) {
                    withSonarQubeEnv('SonarQube') {
                        bat 'mvn -B org.sonarsource.scanner.maven:sonar-maven-plugin:5.1.0.4751:sonar -Dsonar.projectKey=DVP-Week12 -Dsonar.projectName="DVP Week 12"'
                    }
                }
            }
        }

        stage('Quality Gate') {
            steps {
                catchError(
                    buildResult: 'UNSTABLE',
                    stageResult: 'FAILURE'
                ) {
                    timeout(time: 5, unit: 'MINUTES') {
                        waitForQualityGate abortPipeline: true
                    }
                }
            }
        }

        stage('Docker Build') {
            steps {
                catchError(
                    buildResult: 'UNSTABLE',
                    stageResult: 'FAILURE'
                ) {
                    bat 'docker build -t %DOCKERHUB_REPO%:%IMAGE_TAG% .'

                    bat 'docker tag %DOCKERHUB_REPO%:%IMAGE_TAG% %DOCKERHUB_REPO%:latest'
                }
            }
        }

        stage('Push to DockerHub') {
            steps {
                catchError(
                    buildResult: 'UNSTABLE',
                    stageResult: 'FAILURE'
                ) {
                    withCredentials([
                        usernamePassword(
                            credentialsId: 'dockerhub-jenkins-token',
                            usernameVariable: 'DOCKERHUB_USERNAME',
                            passwordVariable: 'DOCKERHUB_PASSWORD'
                        )
                    ]) {

                        powershell '''
                            $ErrorActionPreference = "Stop"

                            $secureToken = ConvertTo-SecureString $env:DOCKERHUB_PASSWORD -AsPlainText -Force
                            $credential = New-Object System.Management.Automation.PSCredential(
                                $env:DOCKERHUB_USERNAME,
                                $secureToken
                            )

                            $credential.GetNetworkCredential().Password |
                                docker login --username $env:DOCKERHUB_USERNAME --password-stdin

                            if ($LASTEXITCODE -ne 0) {
                                throw "Docker Hub login failed."
                            }
                        '''

                        bat 'docker push %DOCKERHUB_REPO%:%IMAGE_TAG%'

                        bat 'docker push %DOCKERHUB_REPO%:latest'

                        bat 'docker logout'
                    }
                }
            }
        }

        stage('Deploy Locally') {
            steps {
                catchError(
                    buildResult: 'UNSTABLE',
                    stageResult: 'FAILURE'
                ) {
                    bat 'docker rm -f %CONTAINER_NAME% 2>NUL || exit /b 0'

                    bat 'docker run -d --name %CONTAINER_NAME% --restart unless-stopped -p 8081:8080 %DOCKERHUB_REPO%:%IMAGE_TAG%'
                }
            }
        }
    }

    post {
        success {
            echo 'Week 12 CI/CD pipeline completed successfully.'
        }

        unstable {
            echo 'Pipeline completed with one or more failed stages. Check Console Output.'
        }

        failure {
            echo 'Pipeline failed. Review the Console Output for details.'
        }

        always {
            echo '========== WEEK 12 PIPELINE SUMMARY =========='
            echo "Build Number: ${BUILD_NUMBER}"
            echo "Docker Image: ${DOCKERHUB_REPO}:${IMAGE_TAG}"
            echo "Application URL: http://localhost:8081"
            echo '=============================================='
        }
    }
}
