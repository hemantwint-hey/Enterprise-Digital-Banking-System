package org.example.enterprisedigitalbankingsystem.kafka.pipeline;

import org.example.enterprisedigitalbankingsystem.kafka.consumer.BeneficiaryEventConsumer;
import org.example.enterprisedigitalbankingsystem.kafka.consumer.TransactionEventConsumer;
import org.example.enterprisedigitalbankingsystem.kafka.consumer.entity.ProcessedEvent;
import org.example.enterprisedigitalbankingsystem.kafka.consumer.repository.ProcessedEventRepository;
import org.example.enterprisedigitalbankingsystem.kafka.event.BeneficiaryActivatedEvent;
import org.example.enterprisedigitalbankingsystem.kafka.event.TransactionCompletedEvent;
import org.example.enterprisedigitalbankingsystem.kafka.producer.BeneficiaryEventProducer;
import org.example.enterprisedigitalbankingsystem.kafka.producer.TransactionEventProducer;
import org.example.enterprisedigitalbankingsystem.notification.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Day 61: Comprehensive End-to-End Event Pipeline Verification Test.
 * Simulates the complete asynchronous path:
 * Producer -> Kafka Topic -> Consumer -> Idempotency Key Guard -> Async Notification -> DLT
 */
@ExtendWith(MockitoExtension.class)
class EndToEndKafkaPipelineVerificationTest {

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @Mock
    private ProcessedEventRepository processedEventRepository;

    @Mock
    private NotificationService notificationService;

    private ObjectMapper objectMapper;
    private TransactionEventProducer transactionProducer;
    private BeneficiaryEventProducer beneficiaryProducer;
    private TransactionEventConsumer transactionConsumer;
    private BeneficiaryEventConsumer beneficiaryConsumer;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();

        transactionProducer = new TransactionEventProducer(kafkaTemplate, objectMapper);
        beneficiaryProducer = new BeneficiaryEventProducer(kafkaTemplate, objectMapper);

        transactionConsumer = new TransactionEventConsumer(objectMapper, processedEventRepository, notificationService);
        beneficiaryConsumer = new BeneficiaryEventConsumer(objectMapper, processedEventRepository, notificationService);
    }

    @Test
    @DisplayName("End-to-End Pipeline: Produce Transaction Event -> Consume -> Idempotency -> Notify -> Persist")
    void verifyEndToEndTransactionPipeline() throws Exception {
        String eventId = UUID.randomUUID().toString();
        TransactionCompletedEvent event = TransactionCompletedEvent.builder()
                .eventId(eventId)
                .transactionId(999L)
                .transactionReference("TXN9999999999999")
                .transactionType("TRANSFER")
                .accountId(42L)
                .accountNumber("ACC-424242")
                .customerId(7L)
                .userId("usr-42")
                .email("alice@bank.com")
                .amount(new BigDecimal("1250.00"))
                .balanceAfterTransaction(new BigDecimal("3750.00"))
                .occurredAt(LocalDateTime.now())
                .build();

        // 1. Stage: Producer publishes to Kafka
        transactionProducer.publish(event);

        ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> payloadCaptor = ArgumentCaptor.forClass(String.class);

        verify(kafkaTemplate, times(1)).send(topicCaptor.capture(), keyCaptor.capture(), payloadCaptor.capture());

        assertThat(topicCaptor.getValue()).isEqualTo("transaction.completed");
        assertThat(keyCaptor.getValue()).isEqualTo("42"); // Account ID used as partition key
        String producedPayload = payloadCaptor.getValue();
        assertThat(producedPayload).contains(eventId);

        // 2. Stage: Consumer receives the event from topic
        when(processedEventRepository.existsByEventId(eventId)).thenReturn(false);

        transactionConsumer.consumeTransactionCompleted(producedPayload, "transaction.completed", 0, 1001L);

        // 3. Stage: Verification of notification dispatch
        ArgumentCaptor<TransactionCompletedEvent> notifCaptor = ArgumentCaptor.forClass(TransactionCompletedEvent.class);
        verify(notificationService, times(1)).sendTransactionNotification(notifCaptor.capture());
        assertThat(notifCaptor.getValue().getEventId()).isEqualTo(eventId);
        assertThat(notifCaptor.getValue().getAmount()).isEqualByComparingTo("1250.00");

        // 4. Stage: Verification of idempotency record insertion
        ArgumentCaptor<ProcessedEvent> processedCaptor = ArgumentCaptor.forClass(ProcessedEvent.class);
        verify(processedEventRepository, times(1)).save(processedCaptor.capture());
        assertThat(processedCaptor.getValue().getEventId()).isEqualTo(eventId);
        assertThat(processedCaptor.getValue().getEventType()).isEqualTo("TRANSACTION_COMPLETED");

        // 5. Stage: Verification of duplicate replay suppression (Redelivery simulation)
        when(processedEventRepository.existsByEventId(eventId)).thenReturn(true);
        transactionConsumer.consumeTransactionCompleted(producedPayload, "transaction.completed", 0, 1002L);

        // Must still only be called once!
        verify(notificationService, times(1)).sendTransactionNotification(any());
        verify(processedEventRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("End-to-End Pipeline: Produce Beneficiary Event -> Consume -> Idempotency -> Notify -> Persist")
    void verifyEndToEndBeneficiaryPipeline() throws Exception {
        String eventId = UUID.randomUUID().toString();
        BeneficiaryActivatedEvent event = BeneficiaryActivatedEvent.builder()
                .eventId(eventId)
                .beneficiaryId(55L)
                .customerId(15L)
                .userId("usr-15")
                .email("charlie@bank.com")
                .nickName("Landlord")
                .accountNumber("ACC-555555")
                .occurredAt(LocalDateTime.now())
                .build();

        // 1. Stage: Producer publishes to Kafka
        beneficiaryProducer.publish(event);

        ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> payloadCaptor = ArgumentCaptor.forClass(String.class);

        verify(kafkaTemplate, times(1)).send(topicCaptor.capture(), keyCaptor.capture(), payloadCaptor.capture());
        assertThat(topicCaptor.getValue()).isEqualTo("beneficiary.activated");
        assertThat(keyCaptor.getValue()).isEqualTo("15"); // Customer ID used as partition key
        String producedPayload = payloadCaptor.getValue();

        // 2. Stage: Consumer processes the published event
        when(processedEventRepository.existsByEventId(eventId)).thenReturn(false);

        beneficiaryConsumer.consumeBeneficiaryActivated(producedPayload, "beneficiary.activated", 0, 501L);

        // 3. Stage: Verification of notification and processed event
        verify(notificationService, times(1)).sendBeneficiaryActivatedNotification(any(BeneficiaryActivatedEvent.class));
        verify(processedEventRepository, times(1)).save(any(ProcessedEvent.class));

        // 4. Stage: Verify duplicate re-send is suppressed
        when(processedEventRepository.existsByEventId(eventId)).thenReturn(true);
        beneficiaryConsumer.consumeBeneficiaryActivated(producedPayload, "beneficiary.activated", 0, 502L);

        verify(notificationService, times(1)).sendBeneficiaryActivatedNotification(any());
        verify(processedEventRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("Pipeline Failure: Malformed Poison Pill -> Consumer re-throws -> DLT Handler safely executes")
    void verifyPoisonPillRoutingAndDltHandling() {
        String poisonPill = "{\"corrupt\": \"data\", [unclosed-array";

        // Consumer must rethrow RuntimeException so Kafka @RetryableTopic / DefaultErrorHandler handles retry
        assertThatThrownBy(() ->
                transactionConsumer.consumeTransactionCompleted(poisonPill, "transaction.completed", 0, 999L))
                .isInstanceOf(RuntimeException.class);

        verify(notificationService, never()).sendTransactionNotification(any());
        verify(processedEventRepository, never()).save(any());

        // When exhausted, message lands in DLT
        assertDoesNotThrow(() ->
                transactionConsumer.handleDeadLetter(poisonPill, "transaction.completed.DLT", "JsonParseException"));
    }
}

