pipeline {
    agent any

    stages {
        stage('Docker Hub Login Test') {
            steps {
                withCredentials([
                    usernamePassword(
                        credentialsId: 'dockerhub-jenkins-token',
                        usernameVariable: 'DOCKERHUB_USERNAME',
                        passwordVariable: 'DOCKERHUB_PASSWORD'
                    )
                ]) {
                  powershell '''
    Write-Host "User: [$env:DOCKERHUB_USERNAME]  Pass length: $($env:DOCKERHUB_PASSWORD.Length)"

    $env:DOCKERHUB_PASSWORD | docker login --username $env:DOCKERHUB_USERNAME --password-stdin

    if ($LASTEXITCODE -ne 0) { throw "Docker Hub login failed." }

    Write-Host "Docker Hub login successful."
'''
                }
            }
        }
    }
}