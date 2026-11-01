# DVP Week 12 – CI/CD with SonarQube and Docker

A small Java 21 HTTP application used to demonstrate Maven tests, SonarQube analysis, Jenkins quality-gate enforcement, Docker packaging, Docker Hub publishing, and local deployment.

## Prerequisites

- Java 21
- Maven 3.x
- Docker Desktop
- Jenkins with Pipeline, Git, GitHub, SonarQube Scanner, and (for `githubPush()`) GitHub plugin support
- SonarQube Community Build
- Docker Hub account

## Run locally

```powershell
mvn clean verify
java -jar target\employee-salary-calculator-1.0.0.jar
```

Open `http://localhost:8080/health` and `http://localhost:8080/salary`.

## SonarQube

Create a SonarQube project with key `DVP-Week12`. In Jenkins, configure a SonarQube installation named `SonarQube`, pointing to `http://localhost:9000`, and store its token in Jenkins credentials. The Jenkinsfile uses `withSonarQubeEnv('SonarQube')` and `waitForQualityGate`.

Configure the SonarQube webhook to reach the Jenkins URL at `/sonarqube-webhook/`. If Jenkins is exposed through Cloudflare Tunnel, use the current public tunnel URL. Keep that tunnel running while analyses execute. The webhook's last-delivery status remains `Never` until an analysis has completed.

## Jenkins setup

1. Create a Pipeline job connected to this GitHub repository and select **Pipeline script from SCM** (Git).
2. Configure a GitHub push webhook to `https://YOUR-JENKINS-PUBLIC-URL/github-webhook/` and enable the GitHub hook trigger in the job if needed.
3. Configure the SonarQube server in **Manage Jenkins → System** with the installation name `SonarQube`.
4. Add a Jenkins username/password credential with ID `dockerhub-credentials` using your Docker Hub username and access token/password.
5. Edit `DOCKERHUB_REPO` in the Jenkinsfile to `YOUR_DOCKERHUB_USERNAME/dvp-week12-app`.
6. Ensure the Jenkins Windows service account can run `mvn`, `docker`, and access the Docker daemon. Configure Maven in PATH or as a Jenkins tool as appropriate.
7. Run the pipeline. The application is deployed locally on port `8081` after a successful quality gate and Docker Hub push.

## Endpoints after deployment

- `http://localhost:8081/health`
- `http://localhost:8081/salary`

## Notes

- The Jenkinsfile is written for a Windows Jenkins agent and uses `bat` commands.
- The `githubPush()` trigger requires the Jenkins GitHub plugin and a GitHub webhook. For a Pipeline job, enable **GitHub hook trigger for GITScm polling** in the job configuration if your Jenkins setup requires it.
- Jenkins must be able to reach SonarQube at `http://localhost:9000`. This assumes Jenkins runs directly on Windows and SonarQube runs in Docker with port 9000 published to the host.
- SonarQube must be able to reach Jenkins through the webhook URL. A Cloudflare Quick Tunnel URL changes when the tunnel is restarted.
- The default SonarQube embedded database is suitable for local evaluation only.
