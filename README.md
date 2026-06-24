# Versioned Key-Value Store

A production-ready version-controlled key-value store built with Java 21, Spring Boot, and PostgreSQL. It exposes a simple HTTP API for storing JSON values and retrieving historical versions by timestamp.

## Features

- **Versioned storage**: Every upsert creates a new version snapshot.
- **Timestamp-based retrieval**: Query the value of a key at any Unix epoch second.
- **Bulk upserts**: Submit multiple key-value pairs in a single request.
- **JSON object values**: Values can be strings, numbers, booleans, arrays, or nested objects.
- **Get all records**: List every current key with its latest value.
- **Containerized**: Docker Compose setup with custom network and resource limits.
- **CI/CD**: GitHub Actions builds, tests, enforces code coverage, and pushes images to GHCR.

## Tech Stack

- Java 21
- Spring Boot 3.3.5
- Spring Data JPA
- PostgreSQL 16
- Maven
- Docker & Docker Compose
- JaCoCo + jacoco-reporter
- k6 (load testing)

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/object` | Store one or more key-value pairs |
| `GET`  | `/object/{key}` | Get the latest value of a key |
| `GET`  | `/object/{key}?timestamp={unixEpochSeconds}` | Get the value at a specific timestamp |
| `GET`  | `/object/get_all_records` | List all current records |

A ready-to-use Bruno collection is available under `bruno/`. Import the folder into Bruno and run the requests against your local or deployed instance.

## Running Locally

### Prerequisites

- JDK 21
- Maven 3.9+
- Docker & Docker Compose (optional)

### With Maven

```bash
mvn -B verify
java -jar target/versioned-kv-store-0.0.1.jar
```

The application expects a PostgreSQL database. Configure it via environment variables or `application.properties`:

```properties
POSTGRES_HOST=localhost
POSTGRES_PORT=5432
POSTGRES_DB=kvdb
POSTGRES_USER=postgres
POSTGRES_PASSWORD=postgres
```

### With Docker Compose

1. Copy and edit the environment file:

```bash
cp .env.example .env
```

2. Start the services:

```bash
docker compose -f docker/docker-compose.yml up -d
```

This starts the application and PostgreSQL on a custom `kv-network`. The application is not exposed on the host by default; use a reverse proxy or attach to the network.

Resource limits are set to `0.5` CPU and `512M` memory per container.

## Testing

Run unit and integration tests:

```bash
mvn -B verify
```

JaCoCo generates a coverage report at `target/site/jacoco/index.html`.

### Load Testing

A k6 load test is included in `k6/load-test.js`. Run it against a running instance:

```bash
k6 run -e BASE_URL=http://localhost:8080 k6/load-test.js
```

## CI / CD

GitHub Actions workflow (`.github/workflows/ci.yml`):

1. **Build & test**: compiles the project, runs tests, and generates a JaCoCo coverage report.
2. **Coverage gate**: uses `jacoco-reporter` to publish coverage as a Check Run. The build fails if line coverage drops below **80%**.
3. **Docker build & push**: on pushes to `master`, builds and pushes the image to GHCR with both `latest` and commit SHA tags.

Coverage reports are uploaded as workflow artifacts.

## Project Structure

```
.
├── .github/workflows/ci.yml
├── docker/
│   ├── Dockerfile
│   └── docker-compose.yml
├── k6/
│   └── load-test.js
├── src/
│   ├── main/java/com/versionedkv/store/
│   │   ├── VersionedKvStoreApplication.java
│   │   ├── kv/
│   │   │   ├── api/KeyValueController.java
│   │   │   ├── dto/KeyValueRecord.java
│   │   │   ├── repository/
│   │   │   │   ├── KeyValueRepository.java
│   │   │   │   ├── KeyValueVersionRepository.java
│   │   │   │   └── custom/impl/KeyValueRepositoryImpl.java
│   │   │   ├── service/impl/KeyValueServiceImpl.java
│   │   │   └── repository/entity/
│   │   └── shared/api/
│   │       ├── ApiResponse.java
│   │       ├── GlobalExceptionHandler.java
│   │       └── NotFoundException.java
│   └── test/java/com/versionedkv/store/
├── bruno/
│   └── object/           # Bruno API collection
├── pom.xml
├── .env.example
└── README.md
```
