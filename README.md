# Banking API — Microservices

A production-grade microservices banking system demonstrating distributed system patterns, event-driven architecture, and cloud-native observability.

[![CI](https://github.com/YOUR_USERNAME/banking-api/actions/workflows/ci.yml/badge.svg)](https://github.com/YOUR_USERNAME/banking-api/actions/workflows/ci.yml)

---

## Architecture

```
┌─────────────────────────────────────────────────┐
│              API Gateway  :8080                 │
│  (Routing · JWT validation · Load balancing)    │
└────────┬──────────┬──────────────┬──────────────┘
         │          │              │
         ▼          ▼              ▼
   ┌──────────┐ ┌──────────┐ ┌───────────────┐
   │  User    │ │ Account  │ │  Transaction  │
   │ Service  │ │ Service  │ │   Service     │
   │  :8081   │ │  :8082   │ │    :8083      │
   └──────────┘ └──────────┘ └───────┬───────┘
                                     │ Kafka
                                     ▼
                             ┌───────────────┐
                             │  Notification │
                             │   Service     │
                             │    :8084      │
                             └───────────────┘

Infrastructure
──────────────
Eureka (Service Discovery)  :8761
Config Server               :8888
PostgreSQL                  :5433
Apache Kafka                :29092
Zipkin (Tracing)            :9411
Prometheus (Metrics)        :9090
Grafana (Dashboards)        :3000
```

---

## Tech Stack

| Layer | Technology |
|---|---|
| Framework | Java 21, Spring Boot 4, Spring Cloud 2025 |
| Service Mesh | Eureka, Spring Cloud Config, Spring Cloud Gateway |
| Communication | OpenFeign (sync), Apache Kafka (async) |
| Resilience | Resilience4j (circuit breaker, retry) |
| Database | PostgreSQL + Flyway migrations |
| Security | Spring Security, JWT (stateless) |
| Observability | Zipkin (tracing), Micrometer + Prometheus + Grafana |
| API Docs | SpringDoc OpenAPI (Swagger UI) |
| CI/CD | GitHub Actions → Docker Hub |
| Cloud | AWS EC2 + Amazon RDS (staging) |

---

## Quick Start

### Prerequisites
- Java 21+
- Docker and Docker Compose
- Maven (wrapper included)

### 1. Start infrastructure

```bash
docker compose up -d
```

Starts PostgreSQL, Kafka, Zipkin, Prometheus, and Grafana.

### 2. Start services (in order)

```bash
# 1 — Config Server (all other services fetch config from here)
cd config-server && ./mvnw spring-boot:run

# 2 — Eureka (service registry)
cd eureka-server && ./mvnw spring-boot:run

# 3 — Core services (separate terminals, order doesn't matter)
cd api-gateway         && ./mvnw spring-boot:run
cd user-service        && ./mvnw spring-boot:run
cd account-service     && ./mvnw spring-boot:run
cd transaction-service && ./mvnw spring-boot:run
cd notification-service && ./mvnw spring-boot:run
```

---

## Observability Dashboards

| Dashboard | URL | What you see |
|---|---|---|
| Swagger UI | http://localhost:8081/swagger-ui.html | All API endpoints with JWT auth |
| Eureka | http://localhost:8761 | All registered services and health |
| Zipkin | http://localhost:9411 | Distributed traces across all services |
| Prometheus | http://localhost:9090 | Raw metrics scrape targets |
| Grafana | http://localhost:3000 | Pre-built dashboards (admin/admin) |

---

## Key Patterns Implemented

- **API Gateway** — single entry point, JWT validation, route protection
- **Service Discovery** — Eureka-based dynamic registration and load balancing
- **Circuit Breaker** — Resilience4j on the Transaction → Account Feign call
- **Event-Driven Architecture** — Kafka async messaging between Transaction and Notification services
- **Distributed Tracing** — Zipkin traces every hop across all services with correlated `traceId`
- **Centralised Config** — Spring Cloud Config Server with per-service properties
- **Database Migrations** — Flyway versioned SQL scripts per service (no `ddl-auto=update`)
- **Observability** — Micrometer metrics scraped by Prometheus, visualised in Grafana
- **Optimistic Locking** — `@Version` on `AccountEntity` prevents concurrent balance corruption

---

## API Documentation

Each service exposes Swagger UI at `/swagger-ui.html`. All endpoints require a `Bearer` JWT token obtained from `POST /api/v1/auth/login`.

Example flow:

```bash
# 1. Register
POST http://localhost:8080/api/v1/auth/register
{"email": "user@bank.com", "password": "secret123"}

# 2. Login → copy the token
POST http://localhost:8080/api/v1/auth/login

# 3. Create account
POST http://localhost:8080/api/v1/accounts
Authorization: Bearer <token>
{"currency": "USD"}

# 4. Transfer funds
POST http://localhost:8080/api/v1/transactions/transfer
Authorization: Bearer <token>
{"sourceAccountId": 1, "destinationAccountId": 2, "amount": 100.00}
```

---

## CI/CD

GitHub Actions runs on every push to `main` and `develop`:

1. **Build** — compiles all services with Maven
2. **Docker** — builds images and pushes to Docker Hub (on `main` only)

Required GitHub secrets: `DOCKER_USERNAME`, `DOCKER_PASSWORD`

---

## Cloud Deployment (AWS)

The project is provisioned on AWS for staging:

- **Compute** — EC2 instance running all services via Docker Compose
- **Database** — Amazon RDS (PostgreSQL), credentials injected via environment variables
- **Config** — `application-prod.properties` in each service reads `${DATABASE_URL}`, `${EUREKA_URL}`, `${ZIPKIN_URL}` etc.

Activate the prod profile:
```bash
java -jar app.jar --spring.profiles.active=prod
```
