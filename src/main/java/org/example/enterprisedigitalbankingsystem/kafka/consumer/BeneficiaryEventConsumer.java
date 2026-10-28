package org.example.enterprisedigitalbankingsystem.kafka.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.enterprisedigitalbankingsystem.kafka.consumer.entity.ProcessedEvent;
import org.example.enterprisedigitalbankingsystem.kafka.consumer.repository.ProcessedEventRepository;
import org.example.enterprisedigitalbankingsystem.kafka.event.BeneficiaryActivatedEvent;
import org.springframework.kafka.annotation.BackOff;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class BeneficiaryEventConsumer {

    private static final String TOPIC = "beneficiary.activated";
    private final ObjectMapper objectMapper;
    private final ProcessedEventRepository processedEventRepository;

    // Day 58: Retry 3 times with exponential backoff (1s -> 2s -> 4s), then route to DLT
    @RetryableTopic(
            attempts = "4",
            backOff = @BackOff(delay = 1000, multiplier = 2.0, maxDelay = 10000),
            autoCreateTopics = "true",
            exclude = {IllegalArgumentException.class, NullPointerException.class}
    )
    @KafkaListener(topics = TOPIC, groupId = "${spring.kafka.consumer.group-id:banking-consumer-group}")
    @Transactional
    public void consumeBeneficiaryActivated(
            String message,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {

        log.info("Received beneficiary event from topic: {}, partition: {}, offset: {}", topic, partition, offset);

        try {
            BeneficiaryActivatedEvent event = objectMapper.readValue(message, BeneficiaryActivatedEvent.class);

            // Day 59 Idempotency Check:
            if (processedEventRepository.existsByEventId(event.getEventId())) {
                log.warn("IDEMPOTENCY TRIGGERED: Event {} already processed. Skipping duplicate.", event.getEventId());
                return; // Safe exit, do not re-process!
            }

            // 1. Execute event business logic (e.g. notify beneficiary, activate payee)
            log.info("Processing beneficiary activated for beneficiaryId: {}, customerId: {}, nickname: {}",
                    event.getBeneficiaryId(), event.getCustomerId(), event.getNickName());

            // 2. Mark event as processed in DB
            ProcessedEvent record = ProcessedEvent.builder()
                    .eventId(event.getEventId())
                    .eventType("BENEFICIARY_ACTIVATED")
                    .processedAt(LocalDateTime.now())
                    .build();
            processedEventRepository.save(record);

            log.info("Successfully processed and recorded event: {}", event.getEventId());

        } catch (Exception e) {
            log.error("Failed to process message from topic: {}", topic, e);
            // Re-throw so @RetryableTopic handles retry & DLT
            throw new RuntimeException(e);
        }
    }

    // Day 58: Handles permanently failed messages landing in Dead Letter Topic
    @DltHandler
    public void handleDeadLetter(
            String failedMessage,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(name = "kafka_exception-message", required = false) String exceptionMsg) {

        log.error("ALERT: Message landed in DLT! Topic: {}, Reason: {}, Payload: {}",
                topic, exceptionMsg, failedMessage);
    }
}

