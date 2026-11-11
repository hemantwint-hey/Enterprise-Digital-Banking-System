# Enterprise Digital Banking System - Learning Progress & Project State

This document is automatically loaded by the AI assistant across all sessions to preserve context, track completed curriculum days, and guide upcoming implementation steps.

---

## 1. Project Overview & Architecture
* **Stack**: Java 17, Spring Boot (4.x / Spring 7), MySQL, Redis, Apache Kafka (KRaft), Spring Security (JWT), Prometheus, Grafana.
* **Package Base**: `org.example.enterprisedigitalbankingsystem`
* **Core Domains**:
  * `auth` / `security`: JWT authentication, role-based access control.
  * `customer`, `account`, `card`, `loan`, `ledger`, `audit`: Core digital banking operations.
  * `transaction`: Deposits, withdrawals, and fund transfers.
  * `beneficiary`: Payee management and activation.
  * `kafka`: Event-driven messaging for transactions and beneficiary activations.
  * `notification`: Asynchronous customer alerts and notification dispatching.
  * `common.logging`: MDC distributed trace correlation and structured Logback configuration.
  * `common.health`: Actuator production health indicators & infrastructure liveness/readiness probes.
  * `monitoring`: Prometheus scrapers and Grafana operational/JVM dashboards.

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
  * **End-to-End Verification Test Suite**: Added 15 comprehensive tests verifying the entire pipeline (Producer -> Serialization -> Broker Topic -> Consumer -> Idempotency Guard -> Notification Dispatch -> Database Record -> Poison Pill Re-throw -> DLT).
* **Day 62: Structured Logging with Logback & MDC Tracing**
  * **Logback Configuration**: Added `src/main/resources/logback-spring.xml` configuring colorized console output, daily rolling file appender (`logs/banking-app.log`, 10MB/file, 30 days history, 1GB cap), dedicated error log file (`logs/banking-app-error.log`, 60 days history), and granular log levels (App: `INFO`, SQL/Hibernate: `DEBUG`, Spring/Kafka: `WARN`).
  * **Distributed MDC Tracing**: Implemented `MdcLoggingFilter` (extracts or generates 16-char `traceId`, returns `X-Correlation-ID` header, sets default `anonymous` user). Attached authenticated email to MDC in `JwtAuthenticationFilter`. Bound Kafka `eventId` and `userId` in `TransactionEventConsumer` and `BeneficiaryEventConsumer`.
* **Day 63: Spring Boot Actuator Production Health & Custom Health Indicators**
  * **Actuator Configuration**: Configured `management.endpoint.health.show-details=always`, `management.endpoint.health.probes.enabled=true` (Liveness & Readiness probes), and app info exposure in `application.properties`.
  * **Security Whitelist**: Permitted `/actuator/health/**` and `/actuator/info` without authentication in `SecurityConfig.java`.
  * **Custom Banking Health Indicator**: Implemented `BankingServiceHealthIndicator` aggregating live connectivity checks for MySQL (`DataSource`), Redis (`RedisConnectionFactory.getConnection().ping()`), and Kafka (`AdminClient.describeCluster()`).
  * **Unit Test Suite**: Created `BankingServiceHealthIndicatorTest` with 4 comprehensive test cases (58 total project tests passing).
* **Day 64: Prometheus Integration & Metrics Scraping**
  * **Dependency**: Added `io.micrometer:micrometer-registry-prometheus` in `pom.xml`.
  * **Security Access**: Configured `/actuator/prometheus` in `SecurityConfig.java` to permit unauthenticated metrics scraping.
  * **Scraper Config**: Created `prometheus.yml` (and `prometheus.yaml`) configuring `banking-app` job to scrape metrics every 5 seconds at `host.docker.internal:8080/actuator/prometheus`.
* **Day 65: Grafana Dashboards & Telemetry Visualization**
  * **Infrastructure Definition**: Added `prometheus` (port 9090) and `grafana` (port 3000) containers into `docker-compose.yaml`.
  * **Auto-Provisioning**: Created `docker/grafana/provisioning/datasources/prometheus-datasource.yml` so Grafana connects to Prometheus out-of-the-box without manual UI setup.
  * **Standard JVM Dashboard**: Community dashboard ID `4701` for JVM heap/threads/GC monitoring.
  * **PromQL KPI Queries**: Defined queries for transaction throughput, HTTP status codes, and Kafka consumer lag.
* **Day 66: Docker (`Dockerfile`) & Distributed Tracing (OpenTelemetry / Zipkin)**
  * **Production Dockerfile**: Created multi-stage `Dockerfile` (`eclipse-temurin:21-jdk-alpine` builder + `eclipse-temurin:21-jre-alpine` runner). Configured non-root `bankingUser`, JVM container flags (`-XX:MaxRAMPercentage=75.0`), Actuator healthcheck, and optimized `.dockerignore`.
  * **Distributed Tracing**: Added `micrometer-tracing-bridge-otel` and `opentelemetry-exporter-zipkin`. Configured 100% trace sampling, Zipkin endpoint (`:9411`), and Kafka W3C trace observation in `application.properties`.
  * **MDC & Tracer Integration**: Harmonized `MdcLoggingFilter` to extract live OpenTelemetry `traceId` from `Tracer.currentSpan()`, injecting `X-Correlation-ID` and `X-Trace-ID` into response headers.
  * **Unit Test Suite**: 60 total project tests passing.
* **Day 67: Docker Compose Multi-Container Orchestration**
  * **Full-Stack Orchestration**: Unified 8 containerized services in `docker-compose.yaml`: MySQL 8.0, Redis 7, Kafka 3.8.0 KRaft, Kafka-UI, Prometheus, Grafana, Zipkin, and the Spring Boot `banking-app`.
  * **Network & DNS Isolation**: Created dedicated `banking-network` bridge for reliable container communication (`redis:6379`, `kafka:29092`, `zipkin:9411`, `mysql:3306`, `banking-app:8080`).
  * **Lifecycle & Health Dependencies**: Enforced startup ordering using `depends_on` with `condition: service_healthy` across database, cache, and message broker prior to launching the core banking engine.
  * **Automated Telemetry**: Configured Grafana dashboard auto-provisioning (`docker/grafana/provisioning/dashboards/dashboards.yml`) and dual-target Prometheus scraping (container + local).
  * **Configuration Template**: Added `.env.example` for zero-configuration deployments.

---

## 3. Next Up on the Roadmap

### 🎯 Immediate Next Step:
* **Day 68: Spring Profiles (Dev & Prod Profiles)**
  * **Base Profile Harmonization**: Refactored `application.properties` to retain universal configurations (Jackson 3, Kafka serializers/idempotency, Project Loom Virtual Threads) with a safe local fallback `spring.profiles.active=dev`.
  * **Development Profile (`application-dev.properties`)**: Configured for local development (`localhost` services, Hibernate `ddl-auto=update`, `show-sql=true`, small HikariCP pool of 10, verbose `DEBUG` logging, and 100% trace sampling).
  * **Production Profile (`application-prod.properties`)**: Hardened for enterprise containers (`validate` DDL, SQL logging disabled, tuned HikariCP pool of 30 with 15s leak detection, restricted Actuator to `health,info,prometheus`, strict externalized `${DB_USERNAME}`, `${DB_PASSWORD}`, and `${JWT_SECRET}` with zero hardcoded fallbacks, Kafka producer linger/batch optimization, and adaptive 10% trace sampling).
  * **Container Orchestration**: Updated `docker-compose.yaml` with `SPRING_PROFILES_ACTIVE=prod` and `.env.example` template.
  * **Unit Test Suite**: Created `SpringProfilesPropertyVerificationTest` with 3 test cases validating profile segregation and properties (63 total project tests passing).
* **Day 69: CI/CD Pipeline (GitHub Actions) & Flyway Database Migration**
  * **Automated CI/CD Pipeline**: Configured `.github/workflows/ci.yml` with dual-stage verification:
    * `build-and-test`: Sets up Java 21 Temurin, Maven dependency caching, compilation, runs all 63 unit/property verification tests, packages production JAR, and uploads Surefire test artifacts.
    * `docker-build-verify`: Leverages Docker Buildx and GitHub Actions layer caching (`gha`) to validate the multi-stage `Dockerfile`.
  * **Live Build Status Badge**: Added official GitHub Actions workflow badge to `README.md`.
  * **Flyway Database Migration**: Added `flyway-core` and `flyway-mysql` dependencies in `pom.xml`, authored `src/main/resources/db/migration/V1__init_banking_schema.sql` defining all 10 core tables with strict relational integrity, and configured `spring.flyway.baseline-on-migrate=true` in `application.properties`.

---

* **Day 70: Database Optimization: Indexes & Constraints & High-Load Benchmarking**
  * **Flyway Migration V2 (`V2__add_indexes_and_constraints.sql`)**: Authored versioned migration script creating 9 high-performance B-Tree & composite indexes and 7 financial CHECK constraints.
  * **Composite B-Tree Indexes**: `idx_accounts_customer_id`, `idx_tx_source_created` `(source_account_id, created_at DESC)`, `idx_tx_dest_created` `(destination_account_id, created_at DESC)`, `idx_ledger_account_created`, `idx_beneficiaries_customer`, `idx_loans_customer`, `idx_emis_loan_status`, `idx_audit_entity_search`, and `idx_processed_events_type_time`.
  * **Strict Financial CHECK Constraints**: Added disk-level integrity checks (`chk_account_balance_non_negative`, `chk_transaction_amount_positive`, `chk_ledger_amount_positive`, `chk_loan_principal_positive`, `chk_loan_interest_non_negative`, `chk_loan_tenure_positive`, `chk_emi_amount_positive`).
  * **JPA Entity Synchronization**: Synchronized `@Table(indexes = { ... })` across `Account`, `Transaction`, `LedgerEntry`, `Beneficiary`, `Loan`, `Emi`, `AuditLog`, and `ProcessedEvent`.
  * **Unit Test Suite**: Added `DatabaseOptimizationMigrationTest` verifying V1 and V2 migration DDL integrity (66 total project tests passing).
  * **High-Throughput Load Testing & Telemetry**:
    * Enhanced `load-test.ps1` with C# high-concurrency engine, real-time progress reporting, and p50/p95/p99 latency distribution tracking.
    * Executed 50,000 requests across 400 concurrent slots: achieved **4,732.38 RPS** (instant peak: **5,202 RPS**) with **100% 200 OK success rate** and 0 errors.
    * Validated full-stack observability in Grafana and Task Manager: CPU saturated at ~95% across all 12 logical processors, JVM platform threads held flat at ~70 via Project Loom Virtual Threads, and heap usage remained stable at ~11.9% (224 MiB).
* **Day 76: Validation (Bean Validation)**
  * **Custom KYC Constraint**: Implemented `@MinimumAge` and `MinimumAgeValidator` enforcing 18+ age verification on `LocalDate` fields while delegating `null` checks cleanly to `@NotNull`.
  * **Global Validation Error Normalization**: Enhanced `ErrorResponse` (`@JsonInclude(NON_NULL)` + `Map<String, String> fieldErrors`, backward-compatible 4-arg constructor) and `GlobalExceptionHandler` to handle `MethodArgumentNotValidException` (field + global `@AssertTrue` errors), `HandlerMethodValidationException` (parameter + cross-parameter errors), `ConstraintViolationException` (leaf property names), `HttpMessageNotReadableException`, and `MethodArgumentTypeMismatchException`.
  * **DTO & Controller Hardening**: Tightened JSR-380 constraints (`@Positive`, `@DecimalMin`/`@DecimalMax`, `@Digits(integer, fraction)`, `@Size`, `@Past`, `@MinimumAge`, cross-field `@JsonIgnore @AssertTrue`) across all 15 active request DTOs, and added `@Validated` + path-variable constraints across all 8 active REST controllers (`AccountController`, `AuditController`, `BeneficiaryController`, `CustomerController`, `EmiController`, `LedgerController`, `LoanController`, `TransactionController`).
  * **Unit Test Suite**: Added `BeanValidationRulesTest` (6 tests) and `GlobalExceptionHandlerValidationTest` (5 tests).

---

## 3. Next Up on the Roadmap

### ⏸️ Deferred Until After Day 79:
* **Days 71–75: Automated Testing Suite** (`docs/roadmap-refs/05-testing.pdf`)
  * Mockito Unit Tests, `@DataJpaTest` Persistence Tests, `@WebMvcTest` Controller Tests, and `@SpringBootTest` End-to-End Integration Tests — paused per user request and scheduled right after Day 79.

### 🎯 Immediate Next Steps (Days 77–79 Notes & Planned Code PDFs Ready):
* **Day 77: Pagination (`Pageable` APIs)** (`docs/2026-11-11-Day77-Pagination-Pageable-APIs.pdf`) — *In Progress (User Implementing)*
  * Skeleton class body created at `src/main/java/.../common/dto/PagedResponse.java`; user implementing `PagedResponse<T>` logic, `@PageableDefault`, `application.properties` bounds, Redis cache key inclusion (`page`/`size`/`sort`), and paginated repository/service/controller rollout.
* **Day 78: Sorting Support** (`docs/2026-11-11-Day78-Sorting-Support.pdf`)
  * `SortValidator` field allowlisting, `PropertyReferenceException` handling, deterministic tie-breakers (`id`/`createdAt`), and cleanup of hardcoded `OrderBy...` repository queries.
* **Day 79: Search (Dynamic Search APIs)** (`docs/2026-11-11-Day79-Dynamic-Search-APIs.pdf`)
  * Type-safe Spring Data JPA `Specification` builders (`Specification.unrestricted()`, `JpaSpecificationExecutor`) for `Transaction`, `Account`, `Customer`, and `AuditLog` with combined Pagination + Sorting.

* **Architectural Initiative: Privacy-Preserving AI Gateway & Spring AI Service**
  * **Decoupled Architecture**: Design and implement the AI layer as an independent microservice communicating asynchronously via Apache Kafka.
  * **ARP-Inspired Identity Abstraction**: Intercept transaction events, strip customer PII (Customer C123 -> Ephemeral Token R789), sanitize/minimize category data, and forward to Spring AI.
  * **AI Smart-Saving Guard**: Use Spring AI to analyze spend categories, identify wasteful recurring subscriptions, and deliver actionable savings insights back to the customer dashboard without leaking identities.

---

## 4. Technical Constraints & Conventions
* **JSON Mapping**: Uses `tools.jackson.databind.ObjectMapper` (Jackson 3).
* **Consumer Group**: `banking-consumer-group` (configured via `${spring.kafka.consumer.group-id}`).
* **Kafka Topics**: `transaction.completed`, `beneficiary.activated`, plus `.DLT` dead-letter topics.
* **Build Command**: `.\mvnw.cmd test-compile` for build verification.
