# Project Memory: Curriculum State & Completed Days

## Project Status: Through Day 64 Completed

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

### What We Need to Do Next:
- **Day 65**: Grafana Dashboards (Docker Grafana on port 3000, connecting Prometheus data source, JVM dashboard ID 4701, custom banking metrics panels).
