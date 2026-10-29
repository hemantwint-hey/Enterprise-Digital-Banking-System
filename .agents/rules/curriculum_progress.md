# Project Memory: Curriculum State & Completed Days

## Project Status: Through Day 62 Completed

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

### What We Need to Do Next:
- **Day 63**: Spring Boot Actuator Production Health (`show-details=always`, `/actuator/health`, custom `BankingServiceHealthIndicator`, Liveness/Readiness probes).
- **Day 64**: Prometheus integration (`micrometer-registry-prometheus`, `/actuator/prometheus`).
- **Day 65**: Grafana dashboards (port 3000, dashboard ID 4701).
