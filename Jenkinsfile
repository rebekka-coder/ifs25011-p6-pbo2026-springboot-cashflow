// CI/CD Cashflow (Spring Boot + Vaadin, Maven, Java 25).
// Catatan: jika konten kursus "Jenkins Grading" menyediakan Jenkinsfile resmi, pakai versi itu.
pipeline {

    agent any

    options {
        timestamps()
        skipDefaultCheckout(true)
        skipStagesAfterUnstable()
    }

    stages {

        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        // Build + unit test + Jacoco (rule coverage 100% ada di pom.xml)
        stage('Build & Test') {
            agent {
                docker {
                    image 'maven:3.9-eclipse-temurin-25'
                    reuseNode true
                }
            }
            steps {
                sh '''
                    set -e
                    echo "=== Maven clean verify (tests + Jacoco) ==="
                    mvn -B -ntp -Dmaven.repo.local=.m2/repository clean verify
                '''
            }
            post {
                always {
                    junit allowEmptyResults: true, testResults: 'target/surefire-reports/*.xml'
                }
            }
        }

        stage('Trivy Security Scan') {
            agent {
                docker {
                    image 'aquasec/trivy:0.74.0'
                    reuseNode true
                    args '''
                        --entrypoint=""
                        -e HOME=/tmp
                        -e XDG_CACHE_HOME=/tmp/.cache
                    '''
                }
            }
            steps {
                sh '''
                    set -e
                    mkdir -p .trivy-cache || true
                    trivy --version
                    trivy fs \
                        --cache-dir .trivy-cache \
                        --scanners vuln \
                        --severity HIGH,CRITICAL \
                        --skip-dirs .m2,target,.trivy-cache,node_modules \
                        --format sarif \
                        --output trivy-results.sarif \
                        --exit-code 1 \
                        .
                    ls -lh trivy-results.sarif
                '''
            }
            post {
                always {
                    recordIssues(
                        enabledForFailure: true,
                        failOnError: false,
                        tools: [
                            sarif(id: 'trivy', name: 'Trivy Security', pattern: 'trivy-results.sarif')
                        ]
                    )
                }
            }
        }

        stage('SonarQube Analysis') {
            agent {
                docker {
                    image 'sonarsource/sonar-scanner-cli:latest'
                    reuseNode true
                    args '--network cicd-network'
                }
            }
            steps {
                withSonarQubeEnv('SonarQube') {
                    sh '''
                        set -e
                        sonar-scanner
                    '''
                }
            }
        }

        stage('Quality Gate') {
            steps {
                timeout(time: 30, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }

        stage('Package Application') {
            agent {
                docker {
                    image 'alpine:3.22'
                    reuseNode true
                    args '-u root'
                }
            }
            steps {
                sh '''
                    set -e
                    apk add --no-cache zip unzip
                    rm -f latest-app.zip
                    zip -r latest-app.zip . \
                        -x "target/*" \
                        -x ".m2/*" \
                        -x ".git/*" \
                        -x ".env" \
                        -x ".env.*" \
                        -x "node_modules/*" \
                        -x ".vaadin/*" \
                        -x ".trivy-cache/*" \
                        -x "latest-app.zip" \
                        -x "trivy-results.sarif"
                    ls -lh latest-app.zip
                    unzip -l latest-app.zip
                '''
            }
        }

        stage('Publish Application') {
            steps {
                archiveArtifacts(artifacts: 'latest-app.zip', fingerprint: true, allowEmptyArchive: false)
                script {
                    def appName = env.JOB_NAME
                        .replaceAll('[^a-zA-Z0-9._-]', '-')
                        .replaceAll('-+', '-')
                        .replaceAll('^-|-$', '')
                    def buildId = env.BUILD_NUMBER
                    echo "Application Name: ${appName}"
                    echo "Build ID: ${buildId}"

                    sh """
                        set -e
                        docker exec cicd-jenkins mkdir -p "/var/jenkins_home/userContent/applications/${appName}/${buildId}"
                        docker cp latest-app.zip "cicd-jenkins:/var/jenkins_home/userContent/applications/${appName}/${buildId}/latest-app.zip"
                        docker exec cicd-jenkins ls -lh "/var/jenkins_home/userContent/applications/${appName}/${buildId}/latest-app.zip"
                    """

                    def jenkinsBaseUrl = env.BUILD_URL
                        .substring(0, env.BUILD_URL.indexOf('/job/'))
                        .replace('localhost', 'host.docker.internal')
                    env.ARTIFACT_URL = "${jenkinsBaseUrl}/userContent/applications/${appName}/${buildId}/latest-app.zip"
                    echo "Artifact URL: ${env.ARTIFACT_URL}"
                }
            }
        }

        // URL_REDEPLOY, URL_PROGRESS, DEPLOY_TOKEN, WEBSITE_ID berasal dari
        // "Prepare an environment for the run" di konfigurasi item Jenkins (langkah 1.3.4).
        stage('Deploy Application') {
            agent {
                docker {
                    image 'curlimages/curl:8.15.0'
                    reuseNode true
                    args '--network cicd-network'
                }
            }
            steps {
                script {
                    echo "=== Request Redeployment ==="
                    writeJSON file: 'deploy-request.json', json: [
                        token_access: env.DEPLOY_TOKEN,
                        website_id  : env.WEBSITE_ID,
                        source_url  : env.ARTIFACT_URL,
                        source_type : 'jenkins'
                    ]
                    def redeployResponse = sh(
                        script: 'curl -sS --fail-with-body -X POST "$URL_REDEPLOY" -H "Content-Type: application/json" -d @deploy-request.json',
                        returnStdout: true
                    ).trim()
                    echo "Redeploy Response: ${redeployResponse}"

                    echo "=== Waiting For Deployment ==="
                    writeJSON file: 'deploy-progress.json', json: [
                        token_access: env.DEPLOY_TOKEN,
                        website_id  : env.WEBSITE_ID
                    ]
                    def maxAttempts = 120
                    def attempt = 0
                    def deploymentStatus = 'IN_PROGRESS'

                    while (deploymentStatus == 'IN_PROGRESS') {
                        attempt++
                        if (attempt > maxAttempts) {
                            error("Deployment timeout. Status masih IN_PROGRESS setelah ${maxAttempts} attempts.")
                        }
                        sleep time: 5, unit: 'SECONDS'
                        echo "=== Checking Deployment Progress (${attempt}/${maxAttempts}) ==="

                        def progressResponse = sh(
                            script: 'curl -sS --fail-with-body -X POST "$URL_PROGRESS" -H "Content-Type: application/json" -d @deploy-progress.json',
                            returnStdout: true
                        ).trim()
                        echo "Progress Response: ${progressResponse}"

                        def json = readJSON text: progressResponse
                        deploymentStatus = json?.data?.status?.toString()?.toUpperCase()
                        if (!deploymentStatus) {
                            error('Response progress tidak memiliki data.status')
                        }
                        echo "Deployment Status: ${deploymentStatus}"

                        if (deploymentStatus == 'SUCCESS') {
                            echo '✅ DEPLOYMENT SUCCESS'
                            break
                        }
                        if (deploymentStatus == 'FAIL') {
                            echo "❌ DEPLOYMENT FAILED"
                            echo (json?.data?.log ?: 'Deployment failed tanpa log.')
                            error("Deployment gagal untuk website ${env.WEBSITE_ID}")
                        }
                    }
                }
            }
            post {
                always {
                    sh 'rm -f deploy-request.json deploy-progress.json'
                }
            }
        }
    }

    post {
        always {
            archiveArtifacts(artifacts: 'trivy-results.sarif', allowEmptyArchive: true)
        }
        success {
            echo "✅ PIPELINE SUCCESS - Artifact: ${env.ARTIFACT_URL ?: '(not published)'}"
        }
        failure {
            echo '❌ PIPELINE FAILED - periksa stage yang merah / Console Output.'
        }
        unstable {
            echo "⚠️ PIPELINE UNSTABLE - Result: ${currentBuild.currentResult}"
        }
    }
}
