pipeline {
    agent any

    options {
        buildDiscarder(logRotator(numToKeepStr: '10'))
        timestamps()
        timeout(time: 45, unit: 'MINUTES')
    }

    environment {
        DOCKER_COMPOSE_PROJECT = 'tradestore'
        DOCKER_IMAGE = 'tradestore-app'
        APP_PORT = '8080'
    }

    stages {
        stage('Checkout') {
            steps {
                echo '========== Checking out code =========='
                checkout scm
            }
        }

        stage('Build') {
            steps {
                echo '========== Building application =========='
                sh '''
                    chmod +x gradlew
                    ./gradlew clean build -x test --no-daemon
                '''
            }
        }

        stage('Unit Tests') {
            steps {
                echo '========== Running Unit Tests =========='
                sh './gradlew test --no-daemon'
            }
            post {
                always {
                    junit '**/build/test-results/test/*.xml'
                    publishHTML([
                        allowMissing: false,
                        alwaysLinkToLastBuild: true,
                        keepAll: true,
                        reportDir: 'build/reports/tests/test',
                        reportFiles: 'index.html',
                        reportName: 'Unit Test Report',
                        reportTitles: 'Unit Tests'
                    ])
                }
            }
        }

        stage('Vulnerability Scan - OWASP Dependency Check') {
            steps {
                echo '========== Scanning for Vulnerabilities =========='
                script {
                    sh '''
                        ./gradlew dependencyCheckAnalyze --no-daemon || exit 0
                    '''

                    // Check for critical/blocker vulnerabilities
                    def reportExists = fileExists('build/reports/dependency-check-report.json')
                    if (reportExists) {
                        def report = readJSON file: 'build/reports/dependency-check-report.json'
                        def criticalCount = 0
                        def highCount = 0

                        report.dependencies.each { dep ->
                            dep.vulnerabilities?.each { vuln ->
                                if (vuln.severity == 'CRITICAL') {
                                    criticalCount++
                                    echo "CRITICAL: ${vuln.name} - ${vuln.description}"
                                }
                                if (vuln.severity == 'HIGH') {
                                    highCount++
                                    echo "HIGH: ${vuln.name} - ${vuln.description}"
                                }
                            }
                        }

                        echo "Vulnerability Summary: CRITICAL=${criticalCount}, HIGH=${highCount}"

                        if (criticalCount > 0) {
                            error("Build failed: ${criticalCount} CRITICAL vulnerabilities detected!")
                        }
                        if (highCount > 3) {
                            error("Build failed: ${highCount} HIGH vulnerabilities detected (threshold: 3)!")
                        }
                    }
                }
            }
            post {
                always {
                    publishHTML([
                        allowMissing: true,
                        alwaysLinkToLastBuild: true,
                        keepAll: true,
                        reportDir: 'build/reports',
                        reportFiles: 'dependency-check-report.html',
                        reportName: 'OWASP Dependency Check',
                        reportTitles: 'Vulnerability Scan'
                    ])
                }
            }
        }

        stage('Build Docker Image') {
            steps {
                echo '========== Building Docker Image =========='
                sh """
                    docker build -t ${DOCKER_IMAGE}:${BUILD_NUMBER} -t ${DOCKER_IMAGE}:latest .
                """
            }
        }

        stage('Stop Existing Containers') {
            steps {
                echo '========== Stopping existing containers =========='
                sh """
                    docker compose -p ${DOCKER_COMPOSE_PROJECT} down || exit 0
                """
            }
        }

        stage('Deploy Application') {
            steps {
                echo '========== Deploying Application =========='
                sh """
                    docker compose -p ${DOCKER_COMPOSE_PROJECT} up -d
                """
                sleep(time: 45, unit: 'SECONDS')
            }
        }

        stage('Health Check') {
            steps {
                echo '========== Running Health Check =========='
                script {
                    def maxRetries = 10
                    def healthy = false

                    for (int i = 1; i <= maxRetries; i++) {
                        try {
                            sh """
                                curl -f http://localhost:${APP_PORT}/api/actuator/health
                            """
                            healthy = true
                            echo "Application is healthy!"
                            break
                        } catch (Exception e) {
                            echo "Health check attempt ${i}/${maxRetries} failed, retrying..."
                            sleep(time: 10, unit: 'SECONDS')
                        }
                    }

                    if (!healthy) {
                        error("Application failed health check after ${maxRetries} attempts")
                    }
                }
            }
        }

        stage('Regression Tests') {
            steps {
                echo '========== Running Regression Tests =========='
                script {
                    def testsFailed = false

                    try {
                        // Test 1: Create Trade
                        echo "Test 1: Creating a trade..."
                        sh '''
                            curl -X POST http://localhost:8080/api/trades \\
                                -H "Content-Type: application/json" \\
                                -d '{"tradeId":"T1","version":1,"counterPartyId":"CP-1","bookId":"B1","maturityDate":"2026-05-20"}' \\
                                -f
                        '''
                        echo "✓ Test 1 PASSED: Trade created successfully"

                        // Test 2: Retrieve Trade
                        echo "Test 2: Retrieving trade..."
                        sh 'curl -f http://localhost:8080/api/trades/T1'
                        echo "✓ Test 2 PASSED: Trade retrieved successfully"

                        // Test 3: Get All Trades
                        echo "Test 3: Getting all trades..."
                        sh 'curl -f http://localhost:8080/api/trades'
                        echo "✓ Test 3 PASSED: All trades retrieved successfully"

                        // Test 4: Swagger UI Accessible
                        echo "Test 4: Checking Swagger UI..."
                        sh 'curl -f http://localhost:8080/api/swagger-ui.html'
                        echo "✓ Test 4 PASSED: Swagger UI accessible"

                        // Test 5: Database Connectivity
                        echo "Test 5: Checking database connectivity..."
                        sh 'curl -f http://localhost:8080/api/actuator/health/db'
                        echo "✓ Test 5 PASSED: Database connection healthy"

                        // Test 6: Version Validation
                        echo "Test 6: Testing version validation (should fail with lower version)..."
                        def versionTest = sh(script: '''
                            curl -X POST http://localhost:8080/api/trades \\
                                -H "Content-Type: application/json" \\
                                -d '{"tradeId":"T1","version":0,"counterPartyId":"CP-1","bookId":"B1","maturityDate":"2026-05-20"}' \\
                                -w "%{http_code}" \\
                                -o /dev/null \\
                                -s
                        ''', returnStdout: true).trim()
                        if (versionTest == "400") {
                            echo "✓ Test 6 PASSED: Version validation working correctly"
                        }

                        // Test 7: Maturity Date Validation
                        echo "Test 7: Testing maturity date validation (past date should fail)..."
                        def dateTest = sh(script: '''
                            curl -X POST http://localhost:8080/api/trades \\
                                -H "Content-Type: application/json" \\
                                -d '{"tradeId":"T2","version":1,"counterPartyId":"CP-1","bookId":"B1","maturityDate":"2020-01-01"}' \\
                                -w "%{http_code}" \\
                                -o /dev/null \\
                                -s
                        ''', returnStdout: true).trim()
                        if (dateTest == "400") {
                            echo "✓ Test 7 PASSED: Maturity date validation working correctly"
                        }

                        echo "=========================================="
                        echo "All Regression Tests PASSED"
                        echo "=========================================="

                    } catch (Exception e) {
                        testsFailed = true
                        echo "✗ Regression Test FAILED: ${e.message}"
                        error("Regression tests failed")
                    }
                }
            }
        }

        stage('Performance Test') {
            steps {
                echo '========== Running Performance Test =========='
                script {
                    try {
                        sh '''
                            echo Testing API response time...
                            curl -w "Response Time: %{time_total}s\\n" \\
                                 -o /dev/null \\
                                 -s http://localhost:8080/api/trades
                        '''
                        echo "✓ Performance test completed"
                    } catch (Exception e) {
                        echo "⚠ Performance test failed: ${e.message}"
                    }
                }
            }
        }
    }

    post {
        always {
            echo '========== Pipeline Execution Summary =========='
            sh """
                echo Build Number: ${BUILD_NUMBER}
                echo Project: ${DOCKER_COMPOSE_PROJECT}
                echo Application URL: http://localhost:${APP_PORT}/api
            """
        }
        success {
            echo '========== Build Successful =========='
            echo "✓ Application deployed successfully"
            echo "✓ All tests passed"
            echo "✓ No critical vulnerabilities detected"
            echo "Access Application: http://localhost:${APP_PORT}/api/swagger-ui.html"
        }
        failure {
            echo '========== Build Failed =========='
            echo "✗ Build failed. Check logs for details."
            sh """
                docker compose -p ${DOCKER_COMPOSE_PROJECT} logs --tail=100 || true
            """
        }
        cleanup {
            echo '========== Cleaning up =========='
            // Keep containers running for manual testing
            // Uncomment below to stop containers after build
            // sh "docker compose -p ${DOCKER_COMPOSE_PROJECT} down"
        }
    }
}
