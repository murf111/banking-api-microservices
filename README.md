# Banking API Microservices

A Spring Boot-based microservices architecture for a banking application. This project uses Spring Cloud (Config Server, Eureka, API Gateway), Kafka for event-driven notifications, Postgres for data persistence, and Zipkin for distributed tracing.

## Prerequisites

- **Java 21+** (project uses features up to Java 24)
- **Docker & Docker Compose**
- **Maven** (wrapper included)

---

## Local Startup Guide

Because this is a distributed system relying on central configuration and service discovery, **boot order is critical**.

### Step 1: Start the Infrastructure

Spin up the backing services (Postgres, Kafka, Zookeeper, and Zipkin) using Docker Compose:

```bash
docker compose up -d
```

Wait a few seconds for the database and Kafka broker to fully initialize before proceeding.

### Step 2: Start the Config Server

All microservices fetch their properties from the Config Server on startup. If this isn't running, the other services will fail to boot.

```bash
cd config-server
./mvnw spring-boot:run
```

Wait until you see `Started ConfigServerApplication` in the console (runs on port 8888).

### Step 3: Start the Eureka Service Registry

Microservices use Eureka to find each other.

```bash
cd eureka-server
./mvnw spring-boot:run
```

Wait until you see `Started EurekaServerApplication` in the console (runs on port 8761).

### Step 4: Start the API Gateway & Core Services

Once Config and Eureka are running, start the remaining services in separate terminal tabs.

**API Gateway** (port 8080 — main entry point for all requests):

```bash
cd api-gateway
./mvnw spring-boot:run
```

**Core Business Services** (order does not strictly matter):

```bash
# User Service (port 8081)
cd user-service
./mvnw spring-boot:run

# Account Service (port 8082)
cd account-service
./mvnw spring-boot:run

# Transaction Service (port 8083)
cd transaction-service
./mvnw spring-boot:run

# Notification Service (port 8084)
cd notification-service
./mvnw spring-boot:run
```

> **Note:** Wait 30–60 seconds after everything starts for the API Gateway to sync its routing table with Eureka before making API requests.

---

## Database Access for Developers

For local debugging, connect directly to the Postgres container using psql:

```bash
docker exec -it banking-api-postgres-1 psql -U myuser -d mydatabase
```

> If your container is named differently, check with `docker ps` and replace `banking-api-postgres-1` with your actual container name.

**Handy psql commands:**

| Command | Description |
|---|---|
| `\dt` | List all tables (e.g., `account_entity`, `transaction_entity`, `users`) |
| `\d table_name` | View the schema of a specific table |
| `SELECT * FROM account_entity;` | View account balances and IDs |
| `\q` | Quit and return to your terminal |

---

## Useful UI Dashboards

Once the system is fully running:

| Dashboard | URL | Purpose |
|---|---|---|
| Eureka Registry | http://localhost:8761 | Verify all 5 services are registered and showing as UP |
| Zipkin Tracing | http://localhost:9411 | View distributed trace IDs and latency across microservice hops |
| API Gateway | http://localhost:8080/api/v1/... | Base URL for all Postman requests |

---

## Testing

To run the automated test suites for all modules:

```bash
./mvnw test
```


CHANGE ON DEPLOY