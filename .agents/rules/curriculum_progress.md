# Project Memory: Curriculum State & Completed Days

## Project Status: Through Day 69 Completed

### History & What We Have Completed:
1. **Days 1–57**: Full Digital Banking System backend:
   - Account, Customer, Transaction, Beneficiary, Ledger, Card, Loan, Audit services.
   - MySQL persistence, Spring Security with JWT, Redis cache.
2. **Day 58: Kafka Error Handling & Dead-Letter Queue (DLQ)**:
   - Declarative `@RetryableTopic` + `@DltHandler` in `TransactionEventConsumer` and `BeneficiaryEventConsumer`.
   - Programmatic `DefaultErrorHandler` + `DeadLetterPublishingRecoverer` in `KafkaConsumerConfig`.
   - Exponential backoff (1s -> 2s -> 4s, max 10s), exclusion of poison pill exceptions.
3. **Day 59: Producer Idempotence & Consumer Deduplication**:
   - `KafkaProducerConfig` idempotence: `enable.idempotence=true`, `acks=all`, `retries=MAX_VALUE`, `max.in.flight.requests.per.connection=5`.
   - Consumer Deduplication: `ProcessedEvent` table + `ProcessedEventRepository` idempotency check + `@Transactional`.
4. **Day 60: Kafka Monitoring, Topic Metrics & Consumer Lag**:
   - `KafkaAdminConfig` (`AdminClient` bean and `@EnableScheduling`).
   - `KafkaLagMonitorService` (computes lag = LEO - committed offset, alerts if lag > 50 messages).
   - `spring-boot-starter-actuator` + Micrometer metrics configuration in `application.properties`.
5. **Day 61: Async Notification Review & End-to-End Pipeline Verification**:
   - Implemented `NotificationService` & `NotificationServiceImpl` for transaction and beneficiary customer alerts.
   - Integrated notification dispatching into `TransactionEventConsumer` and `BeneficiaryEventConsumer` guarded by `existsByEventId()`.
   - Built comprehensive test suite (`TransactionEventConsumerTest`, `BeneficiaryEventConsumerTest`, `NotificationServiceImplTest`, `EndToEndKafkaPipelineVerificationTest`).
6. **Day 62: Structured Logging with Logback & MDC Tracing**:
   - Implemented `src/main/resources/logback-spring.xml` (console colorizing, daily rolling file appender `logs/banking-app.log`, error log `logs/banking-app-error.log`, custom levels).
   - Created `MdcLoggingFilter` for HTTP `X-Correlation-ID` extraction/generation and response header propagation.
   - Wired user email tracking into MDC via `JwtAuthenticationFilter` and eventId/userId into MDC via `TransactionEventConsumer` and `BeneficiaryEventConsumer`.
   - Added `MdcLoggingFilterTest` (54 tests passing).
7. **Day 63: Spring Boot Actuator Production Health & Custom Health Indicators**:
   - Configured `management.endpoint.health.show-details=always`, `management.endpoint.health.probes.enabled=true`, and app metadata in `application.properties`.
   - Whitelisted `/actuator/health/**` and `/actuator/info` in `SecurityConfig.java`.
   - Implemented custom `BankingServiceHealthIndicator` verifying connectivity to MySQL (`DataSource`), Redis (`RedisConnectionFactory`), and Kafka (`AdminClient`).
   - Created `BankingServiceHealthIndicatorTest` (58 total tests passing).
8. **Day 64: Prometheus Integration & Metrics Scraping**:
   - Added `micrometer-registry-prometheus` dependency in `pom.xml`.
   - Whitelisted `/actuator/prometheus` in `SecurityConfig.java`.
   - Configured `prometheus.yml` (and `prometheus.yaml`) with `banking-app` job scraping `host.docker.internal:8080/actuator/prometheus` at 5s intervals.
9. **Day 65: Grafana Dashboards & Telemetry Visualization**:
   - Added `prometheus` (port 9090) and `grafana` (port 3000) services to `docker-compose.yaml`.
   - Created automatic datasource provisioning (`docker/grafana/provisioning/datasources/prometheus-datasource.yml`).
   - Prepared standard JVM Micrometer dashboard (ID 4701) and banking business PromQL queries.
10. **Day 66: Docker (`Dockerfile`) & Distributed Tracing**:
    - Created multi-stage `Dockerfile` (`eclipse-temurin:21` builder + runner) with non-root security user and JVM container memory flags, plus `.dockerignore`.
    - Added `micrometer-tracing-bridge-otel` and `opentelemetry-exporter-zipkin` to `pom.xml`.
    - Added `zipkin` service to `docker-compose.yaml` (port 9411).
    - Configured trace sampling, Zipkin endpoint, and Kafka observation in `application.properties`.
    - Harmonized `MdcLoggingFilter` with Micrometer `Tracer` and updated `MdcLoggingFilterTest` (60 tests passing).
11. **Day 67: Docker Compose Multi-Container Orchestration**:
    - Orchestrated entire 8-service topology in `docker-compose.yaml` (MySQL 8.0, Redis 7, Kafka 3.8.0 KRaft, Kafka-UI, Prometheus, Grafana, Zipkin, Banking App).
    - Established inter-service network `banking-network` with Docker internal DNS resolution.
    - Set up service healthchecks and `depends_on` conditions (`service_healthy`) to guarantee resilient startup ordering.
    - Automated dashboard loading with `docker/grafana/provisioning/dashboards/dashboards.yml`.
    - Added `.env.example` for environment variable templating and dual-target scraping in `prometheus.yml`.
12. **Day 68: Spring Profiles (Dev & Prod Profiles)**:
    - Base `application.properties`: Universal properties, Kafka serializers/idempotency, Project Loom Virtual Threads, fallback `spring.profiles.active=dev`.
    - `application-dev.properties`: Localhost defaults, Hibernate `ddl-auto=update`, `show-sql=true`, HikariCP pool of 10, verbose `DEBUG` logging, 100% trace sampling.
    - `application-prod.properties`: Hardened container profile (`ddl-auto=validate`, SQL logging disabled, tuned HikariCP pool of 30 with 15s leak detection, restricted Actuator, strict externalized secrets without defaults, Kafka batch optimization, adaptive 10% trace sampling).
    - Added `SPRING_PROFILES_ACTIVE=prod` to `docker-compose.yaml` and `.env.example`.
    - Created `SpringProfilesPropertyVerificationTest` with 3 test cases (63 total project tests passing).

13. **Day 69: CI/CD Pipeline (GitHub Actions) & Flyway Database Migration**:
    - Created `.github/workflows/ci.yml` with automated Java 21 build, Maven dependency caching, 63 automated tests, and Docker container build verification.
    - Added live build status badge to `README.md`.
    - Integrated Flyway database migration (`flyway-core`, `flyway-mysql`, `V1__init_banking_schema.sql` for 10 entities, and `spring.flyway.baseline-on-migrate=true`).
    - Verified property tests passing with `.\mvnw.cmd test -Dtest=SpringProfilesPropertyVerificationTest`.

### What We Need to Do Next:
- **Day 70: Kubernetes Deployment (K8s Manifests: Deployments, Services, ConfigMaps, Secrets, Probes)**:
  - Create Kubernetes manifests for the Digital Banking System (`k8s/deployment.yaml`, `k8s/service.yaml`, `k8s/configmap.yaml`, `k8s/secret.yaml`).
  - Wire Spring Boot Actuator `/actuator/health/liveness` and `/readiness` probes directly into Kubernetes container health checks.
