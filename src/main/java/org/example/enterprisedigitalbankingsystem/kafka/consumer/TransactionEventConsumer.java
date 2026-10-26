package org.example.enterprisedigitalbankingsystem.kafka.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.enterprisedigitalbankingsystem.kafka.consumer.repository.ProcessedEventRepository;
import org.example.enterprisedigitalbankingsystem.kafka.event.TransactionCompletedEvent;
import org.springframework.kafka.annotation.BackOff;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
@Slf4j
public class TransactionEventConsumer {

    private static final String TOPIC = "transaction.completed";
    private final ObjectMapper objectMapper;
    private final ProcessedEventRepository processedEventRepository;

    /*
     * TODO (Day 58):
     * Add @RetryableTopic with exponential backoff & DLT configuration:
     * @RetryableTopic(
     *     attempts = "4",
     *     backoff = @Backoff(delay = 1000, multiplier = 2.0),
     *     autoCreateTopics = "true"
     *
     */
    @RetryableTopic(
            attempts = "4",
            backOff = @BackOff(delay = 1000, multiplier = 2.0, maxDelay = 10000),
            autoCreateTopics = "true",
            exclude = {IllegalArgumentException.class, NullPointerException.class}
    )
    @KafkaListener(topics = TOPIC, groupId = "${spring.kafka.consumer.group-id:banking-consumer-group}")
    public void consumeTransactionCompleted(
            String message,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {

        log.info("Received event from topic: {}, partition: {}, offset: {}", topic, partition, offset);

        try {
            TransactionCompletedEvent event = objectMapper.readValue(message, TransactionCompletedEvent.class);
            if(processedEventRepository.existsByEventId(event.getEventId())){
                log.warn("IDEMPOTENCY TRIGGERED: Event {} already processed . Skipping duplicate",event.getEventId());
                return;
            }
            log.info("Processing transaction completed for account : {}, amount : {}", event.getAccountNumber(), event.getAmount());
        } catch (Exception e) {
            log.error("Failed to process message from topic: {}", topic, e);
            // Re-throw if you want Spring Kafka error handler / @RetryableTopic to retry
            throw new RuntimeException("Error processing transaction event", e);
        }
    }
    @DltHandler
    public void handleDeadLetter(
            String failedMessaage,
            @Header(KafkaHeaders.RECEIVED_TOPIC)String topic,
            @Header(name = "kafka_exception-message",required = false)String exceptionMsg){
        log.error("ALERT: Message landed in DLT! TOpic: {}, Reason: {}, Payload: {}",topic,exceptionMsg,failedMessaage);
    }
}
