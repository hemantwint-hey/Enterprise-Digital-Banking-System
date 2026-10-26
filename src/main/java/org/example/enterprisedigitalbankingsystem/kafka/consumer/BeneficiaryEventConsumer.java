package org.example.enterprisedigitalbankingsystem.kafka.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.enterprisedigitalbankingsystem.kafka.consumer.repository.ProcessedEventRepository;
import org.example.enterprisedigitalbankingsystem.kafka.event.BeneficiaryActivatedEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
@Slf4j
public class BeneficiaryEventConsumer {

    private static final String TOPIC = "beneficiary.activated";
    private final ObjectMapper objectMapper;
    private final ProcessedEventRepository processedEventRepository;

    /*
     * TODO (Day 58):
     * Add @RetryableTopic with exponential backoff & DLT configuration:
     * @RetryableTopic(
     *     attempts = "4",
     *     backoff = @Backoff(delay = 1000, multiplier = 2.0),
     *     autoCreateTopics = "true"
     * )
     */
    @KafkaListener(topics = TOPIC, groupId = "${spring.kafka.consumer.group-id:banking-consumer-group}")
    public void consumeBeneficiaryActivated(
            String message,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {

        log.info("Received event from topic: {}, partition: {}, offset: {}", topic, partition, offset);

        try {
            BeneficiaryActivatedEvent event = objectMapper.readValue(message, BeneficiaryActivatedEvent.class);

            /*
             * TODO (Day 59): Idempotency check using processedEventRepository
             * if (processedEventRepository.existsByEventId(event.getEventId())) {
             *     log.warn("Duplicate event detected: {}. Skipping.", event.getEventId());
             *     return;
             * }
             *
             * Process business logic here...
             *
             * Save to processed_events table:
             * processedEventRepository.save(new ProcessedEvent(...));
             */

        } catch (Exception e) {
            log.error("Failed to process message from topic: {}", topic, e);
            throw new RuntimeException("Error processing beneficiary event", e);
        }
    }

    /*
     * TODO (Day 58):
     * Add Dead-Letter Queue handler method:
     * @DltHandler
     * public void handleDeadLetter(String failedPayload, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
     *     log.error("Message landed in DLT: Topic={}, Payload={}", topic, failedPayload);
     * }
     */
}
