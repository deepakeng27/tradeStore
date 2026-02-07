# Trade Store Application

A comprehensive Spring Boot application for managing financial trades with version control, maturity date validation, event streaming via Kafka, and dual database support (PostgreSQL + MongoDB).

## Project Overview

The Trade Store application handles thousands of incoming trades and stores them with:
- **Version Control**: Rejects lower versions, accepts same/higher versions
- **Maturity Date Validation**: Rejects past dates, marks trades as expired when maturity date passes
- **Event Streaming**: Publishes trade events to Kafka topics
- **Dual Database**: PostgreSQL for transactional data, MongoDB for audit trails
- **API Documentation**: Swagger UI for interactive API testing
- **Comprehensive Testing**: JUnit 5 tests with TDD approach
- **CI/CD Pipeline**: Jenkins pipeline with vulnerability scanning

## Technology Stack

- **Language**: Java 21
- **Framework**: Spring Boot 4.0.2
- **Databases**: PostgreSQL 15, MongoDB 6.0
- **Message Broker**: Apache Kafka 7.5.0
- **Testing**: JUnit 5, Mockito, TestContainers
- **API Docs**: SpringDoc OpenAPI (Swagger)
- **Build Tool**: Gradle
- **Containerization**: Docker, Docker Compose
- **CI/CD**: Jenkins

## Prerequisites

- Java 21
- Docker & Docker Compose
- Git
- Gradle (or use ./gradlew)
- Jenkins (for CI/CD pipeline)

## Getting Started

### 1. Clone the Repository

```bash
git clone https://github.com/your-repo/tradeStore.git
cd tradeStore
```

### 2. Start Infrastructure Services

Use Docker Compose to start PostgreSQL, MongoDB, and Kafka:

```bash
docker-compose up -d
```

This will start:
- **PostgreSQL**: localhost:5432 (user: postgres, password: postgres)
- **MongoDB**: localhost:27017 (user: mongo, password: mongo)
- **Kafka**: localhost:9092
- **Trade Store App**: localhost:8080

### 3. Build the Application

```bash
./gradlew clean build
```

### 4. Run the Application

**Option A: Using Gradle**
```bash
./gradlew bootRun
```

**Option B: Using Docker**
```bash
docker-compose up -d tradestore-app
```

### 5. Access the Application

- **Swagger UI**: http://localhost:8080/api/swagger-ui.html
- **API Docs**: http://localhost:8080/api/v3/api-docs
- **Health Check**: http://localhost:8080/api/trades

## API Endpoints

### Create/Update Trade
```
POST /api/trades
Content-Type: application/json

{
  "tradeId": "T1",
  "version": 1,
  "counterPartyId": "CP-1",
  "bookId": "B1",
  "maturityDate": "2026-05-20"
}

Response: 201 Created
{
  "id": 1,
  "tradeId": "T1",
  "version": 1,
  "counterPartyId": "CP-1",
  "bookId": "B1",
  "maturityDate": "2026-05-20",
  "createdDate": "2026-02-07T11:46:38.107",
  "updatedDate": "2026-02-07T11:46:38.107",
  "expired": false,
  "status": "ACTIVE"
}
```

### Get Trade by ID
```
GET /api/trades/{tradeId}

Response: 200 OK
{
  "id": 1,
  "tradeId": "T1",
  "version": 1,
  ...
}
```

### Get All Trades
```
GET /api/trades

Response: 200 OK
[
  { trade1 },
  { trade2 }
]
```

### Mark Trade as Expired
```
PUT /api/trades/{tradeId}/expire

Response: 200 OK
{
  "id": 1,
  "tradeId": "T1",
  "expired": true,
  "status": "EXPIRED",
  "expiryDate": "2026-02-07"
}
```

### Get Audit Trail
```
GET /api/trades/{tradeId}/audit

Response: 200 OK
[
  {
    "id": "...",
    "tradeId": "T1",
    "version": 1,
    "action": "CREATE",
    "reason": "Trade processed successfully",
    "auditTimestamp": "2026-02-07T11:46:38.107",
    "status": "ACTIVE"
  }
]
```

## Validation Rules

### 1. Version Control
- Rejects trades with **lower** version than existing
- Accepts trades with **same** or **higher** version (replaces)
- Error: `409 Conflict` with message about version mismatch

### 2. Maturity Date Validation
- Rejects trades with maturity date **before** today
- Accepts trades with maturity date **today or in future**
- Error: `400 Bad Request` when maturity date is invalid

### 3. Trade Expiration
- Automatically marks trades as expired when maturity date passes
- Use `PUT /api/trades/{tradeId}/expire` endpoint
- Status changes to `EXPIRED` with expiry date recorded

## Kafka Topics

### trade-events
- Triggered on: CREATE actions
- Partitions: 3
- Retention: 7 days
- Message Format: JSON with trade details

### trade-updates
- Triggered on: UPDATE, EXPIRE actions
- Partitions: 3
- Retention: 7 days

## Testing

### Run All Tests
```bash
./gradlew test
```

### Run Specific Test Class
```bash
./gradlew test --tests TradeServiceTest
```

### View Test Report
```bash
# After running tests
open build/reports/tests/test/index.html
```

### Test Coverage
Tests are organized as:
- **Unit Tests**: Validators, Services, Controllers
- **Integration Tests**: Repository layer
- **Controller Tests**: MockMvc with mocked services

Target Coverage: >80% (Unit + Integration)

## Architecture Diagrams

PlantUML diagrams are available in `docs/diagrams/`:

1. **Trade_Store_Class_Diagram.puml** - Class and component relationships
2. **Trade_Processing_Sequence.puml** - Trade processing flow
3. **Trade_Store_Architecture.puml** - Overall system architecture

Generate PNG/SVG:
```bash
# Using PlantUML CLI
plantuml docs/diagrams/*.puml

# Or online at: https://www.plantuml.com/plantuml/uml/
```

## Configuration

### application.properties

```properties
# Server
server.port=8080

# PostgreSQL
spring.datasource.url=jdbc:postgresql://localhost:5432/tradestore
spring.datasource.username=postgres
spring.datasource.password=postgres

# MongoDB
spring.data.mongodb.uri=mongodb://localhost:27017/tradestore

# Kafka
spring.kafka.bootstrap-servers=localhost:9092
spring.kafka.consumer.group-id=trade-store-group

# Swagger
springdoc.swagger-ui.path=/swagger-ui.html
```

## Jenkins CI/CD Pipeline

### Setup Jenkins

1. **Create New Pipeline Job**
   - Source: GitHub repository
   - Pipeline script from SCM
   - Script path: Jenkinsfile

2. **Configure Environment Variables**
   ```
   SONARQUBE_URL = http://sonarqube:9000
   SONARQUBE_TOKEN = your-sonarqube-token
   BUILD_NOTIFICATION_EMAIL = your-email@company.com
   ```

3. **Install Required Plugins**
   - Pipeline
   - Git
   - SonarQube Scanner
   - Email Extension
   - HTML Publisher

### Pipeline Stages

1. **Checkout** - Clones the repository
2. **Build** - Compiles the project
3. **Unit Tests** - Runs JUnit tests
4. **Code Quality Analysis** - SonarQube scan
5. **Vulnerability Scan** - OWASP Dependency-Check for CVEs
6. **Build Docker Image** - Creates container image
7. **Integration Tests** - Tests with Docker Compose
8. **Publish Artifacts** - Archives JAR file
9. **Deployment** - Deploys to production

### Triggering the Pipeline

```bash
# Manual trigger
# Push to main branch automatically triggers

# View logs
# Jenkins UI: Manage Jenkins > View Logs
```

## Vulnerability Scanning

The pipeline includes **OWASP Dependency-Check** for CVE scanning:

- Scans all dependencies
- Fails build on **CRITICAL** or **HIGH** vulnerabilities
- Generates HTML report

To run locally:
```bash
./dependency-check/bin/dependency-check.sh --scan build/libs/ --format HTML
```

## Troubleshooting

### 1. PostgreSQL Connection Error
```
Error: connection refused
Solution: 
- Ensure PostgreSQL is running: docker-compose ps
- Check credentials in application.properties
```

### 2. MongoDB Connection Error
```
Error: could not resolve 'mongodb'
Solution:
- Start MongoDB: docker-compose up mongodb
- Verify connection string
```

### 3. Kafka Connection Error
```
Error: java.net.ConnectException
Solution:
- Start Kafka: docker-compose up kafka zookeeper
- Wait for Kafka to be ready (health check)
```

### 4. Version Validation Error
```
Error: Trade T1 rejected: incoming version 1 is lower than existing version 2
Solution:
- Send version >= 2 for this trade
```

### 5. Maturity Date Validation Error
```
Error: Trade T1 rejected: maturity date 2026-01-01 is before today's date
Solution:
- Use maturity date >= today
```

## Project Structure

```
tradeStore/
├── src/
│   ├── main/
│   │   ├── java/com/dws/trade/tradestore/
│   │   │   ├── controller/         # REST endpoints
│   │   │   ├── service/            # Business logic
│   │   │   ├── repository/         # Data access
│   │   │   ├── model/              # Entity classes
│   │   │   ├── dto/                # Data transfer objects
│   │   │   ├── validator/          # Validation logic
│   │   │   ├── exception/          # Custom exceptions
│   │   │   └── config/             # Configuration classes
│   │   └── resources/
│   │       └── application.properties
│   └── test/
│       └── java/com/dws/trade/tradestore/
│           ├── controller/
│           ├── service/
│           ├── validator/
│           └── repository/
├── docs/
│   └── diagrams/                   # PlantUML diagrams
├── Dockerfile
├── docker-compose.yml
├── Jenkinsfile                     # CI/CD pipeline
├── build.gradle
└── README.md
```

## Key Features

✅ **Trade Version Control** - Prevents lower version updates
✅ **Maturity Date Validation** - Ensures future dates
✅ **Auto Expiration** - Marks trades as expired
✅ **Kafka Event Streaming** - Async event processing
✅ **Dual Database** - PostgreSQL + MongoDB
✅ **Audit Trail** - MongoDB audit logs
✅ **Swagger API Docs** - Interactive API testing
✅ **Comprehensive Tests** - >80% code coverage
✅ **Docker Support** - Easy containerization
✅ **Jenkins Pipeline** - Automated CI/CD
✅ **Vulnerability Scanning** - CVE detection

## Contributing

1. Create a feature branch
2. Write tests first (TDD)
3. Implement feature
4. Ensure all tests pass
5. Submit pull request

## License

Apache License 2.0

## Contact

For questions, contact the Trade Team at trade@dws.com
