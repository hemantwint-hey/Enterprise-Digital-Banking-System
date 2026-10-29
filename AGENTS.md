# Enterprise Digital Banking System - Learning Progress & Project State

This document is automatically loaded by the AI assistant across all sessions to preserve context, track completed curriculum days, and guide upcoming implementation steps.

---

## 1. Project Overview & Architecture
* **Stack**: Java 17, Spring Boot (4.x / Spring 7), MySQL, Redis, Apache Kafka (KRaft), Spring Security (JWT).
* **Package Base**: `org.example.enterprisedigitalbankingsystem`
* **Core Domains**:
  * `auth` / `security`: JWT authentication, role-based access control.
  * `customer`, `account`, `card`, `loan`, `ledger`, `audit`: Core digital banking banking operations.
  * `transaction`: Deposits, withdrawals, and fund transfers.
  * `beneficiary`: Payee management and activation.
  * `kafka`: Event-driven messaging for transactions and beneficiary activations.
  * `notification`: Asynchronous customer alerts and notification dispatching.
  * `common.logging`: MDC distributed trace correlation and structured Logback configuration.

---

## 2. Curriculum Roadmap & Completed Progress

### ✅ Completed Days:
* **Days 1–57**: Core banking architecture, Domain models, REST APIs, JPA persistence, MySQL transactional ledger, Redis caching, and initial Kafka producer/consumer skeleton.
* **Day 58: Kafka Error Handling & Dead-Letter Queue (DLQ)**
  * **Declarative Approach**: `@RetryableTopic` configured on `TransactionEventConsumer` and `BeneficiaryEventConsumer` (4 attempts, exponential backoff: 1s -> 2s -> 4s, `maxDelay = 10000`, excluding `IllegalArgumentException` and `NullPointerException`).
  * **Dead-Letter Handler**: `@DltHandler` handles poison pills landing in `<topic>.DLT`.
  * **Programmatic Approach**: `KafkaConsumerConfig` defines `DefaultErrorHandler` with `DeadLetterPublishingRecoverer` and `AckMode.RECORD`.
* **Day 59: Producer Idempotence & Consumer Deduplication**
  * **Producer Idempotency**: Configured in `KafkaProducerConfig` and `application.properties` (`enable.idempotence=true`, `acks=all`, `retries=MAX_VALUE`, `max.in.flight.requests.per.connection=5`).
  * **Consumer Deduplication**: Idempotency Key Guard using `ProcessedEvent` entity (`processed_events` table) and `ProcessedEventRepository.existsByEventId()`. Wrapped in `@Transactional` to guarantee exactly-once consumer execution.
* **Day 60: Kafka Monitoring, Topic Metrics & Consumer Lag**
  * **KafkaAdminConfig**: Exposes `AdminClient` bean and enables `@EnableScheduling`.
  * **KafkaLagMonitorService**: Dynamically computes partition Log End Offset (LEO) minus committed offset via `AdminClient`. Periodically checks lag every 30s (`@Scheduled(fixedRate = 30000)`) and warns if lag > 50 messages.
  * **Actuator & Micrometer**: Added `spring-boot-starter-actuator` in `pom.xml`, enabled health/metrics/prometheus endpoints, and activated Kafka consumer/producer Micrometer auto-registration.
* **Day 61: Async Notification Review & End-to-End Pipeline Verification**
  * **Async Notification Service**: Implemented `NotificationService` and `NotificationServiceImpl` for dispatching transaction and beneficiary activation customer alerts.
  * **Consumer Notification Integration**: Wired `NotificationService` into `TransactionEventConsumer` and `BeneficiaryEventConsumer`, triggered only after passing the Idempotency Key Guard (`ProcessedEventRepository.existsByEventId()`).
  * **End-to-End Verification Test Suite**: Added 15 comprehensive tests (`TransactionEventConsumerTest`, `BeneficiaryEventConsumerTest`, `NotificationServiceImplTest`, `EndToEndKafkaPipelineVerificationTest`) verifying the entire pipeline (Producer -> Serialization -> Broker Topic -> Consumer -> Idempotency Guard -> Notification Dispatch -> Database Record -> Poison Pill Re-throw -> DLT).
* **Day 62: Structured Logging with Logback & MDC Tracing**
  * **Logback Configuration**: Added `src/main/resources/logback-spring.xml` configuring colorized console output, daily rolling file appender (`logs/banking-app.log`, 10MB/file, 30 days history, 1GB cap), dedicated error log file (`logs/banking-app-error.log`, 60 days history), and granular log levels (App: `INFO`, SQL/Hibernate: `DEBUG`, Spring/Kafka: `WARN`).
  * **Distributed MDC Tracing**: Implemented `MdcLoggingFilter` (extracts or generates 16-char `traceId`, returns `X-Correlation-ID` header, sets default `anonymous` user). Attached authenticated email to MDC in `JwtAuthenticationFilter`. Bound Kafka `eventId` and `userId` in `TransactionEventConsumer` and `BeneficiaryEventConsumer`.
  * **Test Coverage**: Added `MdcLoggingFilterTest` (54 total unit tests passing).

---

## 3. Next Up on the Roadmap

### 🎯 Immediate Next Step:
* **Day 63: Spring Boot Actuator Production Health & Custom Health Indicators**
  * Deep dive into Actuator endpoints: `/actuator/health`, `/actuator/info`, `/actuator/metrics`.
  * Configure `management.endpoint.health.show-details=always` and `management.endpoint.health.probes.enabled=true` (Liveness & Readiness probes).
  * Implement custom `BankingServiceHealthIndicator` (checks critical dependencies: DB connectivity, Redis cache latency, Kafka broker status, and ledger balance integrity).

### 📅 Upcoming Days:
* **Day 64**: Prometheus Integration (`micrometer-registry-prometheus`, `/actuator/prometheus`, `prometheus.yml` scraper configuration).
* **Day 65**: Grafana Dashboards (Docker Grafana on port 3000, importing JVM/Micrometer Dashboard ID 4701 and custom banking KPIs).

---

## 4. Technical Constraints & Conventions
* **JSON Mapping**: Uses `tools.jackson.databind.ObjectMapper` (Jackson 3).
* **Consumer Group**: `banking-consumer-group` (configured via `${spring.kafka.consumer.group-id}`).
* **Kafka Topics**: `transaction.completed`, `beneficiary.activated`, plus `.DLT` dead-letter topics.
* **Build Command**: `.\mvnw.cmd test-compile` for build verification.
