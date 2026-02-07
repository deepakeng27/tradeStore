pipeline {
    agent any

    options {
        buildDiscarder(logRotator(numToKeepStr: '10'))
        timestamps()
        timeout(time: 30, unit: 'MINUTES')
    }

    environment {
        DOCKER_REGISTRY = 'docker.io'
        DOCKER_IMAGE = 'tradestore-app'
        GIT_COMMIT_MSG = sh(script: "git log -1 --pretty=%B", returnStdout: true).trim()
    }

    stages {
        stage('Checkout') {
            steps {
                echo '========== Checking out code =========='
                checkout scm
                sh 'git log -1 --oneline'
            }
        }

        stage('Build') {
            steps {
                echo '========== Building application =========='
                sh './gradlew clean build -x test'
            }
        }

        stage('Unit Tests') {
            steps {
                echo '========== Running Unit Tests =========='
                sh './gradlew test'
            }
            post {
                always {
                    junit 'build/test-results/test/**/*.xml'
                    publishHTML([
                        reportDir: 'build/reports/tests/test',
                        reportFiles: 'index.html',
                        reportName: 'Unit Test Report'
                    ])
                }
            }
        }

        stage('Code Quality Analysis') {
            steps {
                echo '========== Running SonarQube Analysis =========='
                sh '''
                    ./gradlew sonarqube \
                        -Dsonar.projectKey=trade-store \
                        -Dsonar.sources=src/main \
                        -Dsonar.tests=src/test \
                        -Dsonar.host.url=${SONARQUBE_URL} \
                        -Dsonar.login=${SONARQUBE_TOKEN}
                '''
            }
        }

        stage('Vulnerability Scan') {
            steps {
                echo '========== Scanning for CVEs =========='
                sh '''
                    # Install OWASP Dependency-Check if not present
                    if [ ! -d "dependency-check" ]; then
                        curl -L https://github.com/jeremylong/DependencyCheck_Plugin/releases/download/v8.2.1/dependency-check-8.2.1-release.zip -o dependency-check.zip
                        unzip dependency-check.zip
                        rm dependency-check.zip
                    fi

                    # Run vulnerability scan
                    ./dependency-check/bin/dependency-check.sh \
                        --scan build/libs/ \
                        --format JSON \
                        --project "Trade Store" || true

                    # Check for critical vulnerabilities
                    if grep -q '"severity":"CRITICAL"' dependency-check-report.json || \
                       grep -q '"severity":"HIGH"' dependency-check-report.json; then
                        echo "Critical or High vulnerabilities detected!"
                        exit 1
                    fi
                '''
            }
            post {
                always {
                    publishHTML([
                        reportDir: '.',
                        reportFiles: 'dependency-check-report.html',
                        reportName: 'Vulnerability Scan Report'
                    ])
                }
            }
        }

        stage('Build Docker Image') {
            steps {
                echo '========== Building Docker Image =========='
                sh '''
                    docker build -t ${DOCKER_IMAGE}:${BUILD_NUMBER} .
                    docker tag ${DOCKER_IMAGE}:${BUILD_NUMBER} ${DOCKER_IMAGE}:latest
                '''
            }
        }

        stage('Integration Tests') {
            steps {
                echo '========== Running Integration Tests =========='
                sh '''
                    # Start Docker containers
                    docker-compose up -d

                    # Wait for services to be healthy
                    sleep 30

                    # Run integration tests
                    ./gradlew integrationTest || TEST_RESULT=$?

                    # Stop Docker containers
                    docker-compose down

                    exit ${TEST_RESULT:-0}
                '''
            }
            post {
                always {
                    junit 'build/test-results/integrationTest/**/*.xml'
                }
            }
        }

        stage('Publish Artifacts') {
            when {
                branch 'main'
            }
            steps {
                echo '========== Publishing Artifacts =========='
                sh '''
                    # Archive build artifacts
                    mkdir -p artifacts
                    cp build/libs/*.jar artifacts/

                    # Optional: Push to artifact repository
                    # artifactoryPublish()
                '''
            }
            post {
                success {
                    archiveArtifacts artifacts: 'artifacts/**/*.jar'
                }
            }
        }

        stage('Deployment') {
            when {
                branch 'main'
            }
            steps {
                echo '========== Deploying Application =========='
                sh '''
                    # Deploy using docker-compose or Kubernetes
                    docker-compose up -d

                    # Health check
                    sleep 10
                    curl -f http://localhost:8080/api/trades || exit 1
                '''
            }
        }
    }

    post {
        always {
            echo '========== Cleaning up =========='
            cleanWs()
        }
        success {
            echo '========== Build Successful =========='
            // Send success notification
            emailext(
                subject: "Build Successful: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                body: "Build ${env.BUILD_NUMBER} completed successfully",
                to: "${BUILD_NOTIFICATION_EMAIL}"
            )
        }
        failure {
            echo '========== Build Failed =========='
            // Send failure notification
            emailext(
                subject: "Build Failed: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                body: "Build ${env.BUILD_NUMBER} failed. Check logs for details.",
                to: "${BUILD_NOTIFICATION_EMAIL}"
            )
        }
    }
}
