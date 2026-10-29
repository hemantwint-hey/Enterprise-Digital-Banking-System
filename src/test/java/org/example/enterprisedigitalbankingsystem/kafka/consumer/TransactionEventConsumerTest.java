package org.example.enterprisedigitalbankingsystem.kafka.consumer;

import org.example.enterprisedigitalbankingsystem.kafka.consumer.entity.ProcessedEvent;
import org.example.enterprisedigitalbankingsystem.kafka.consumer.repository.ProcessedEventRepository;
import org.example.enterprisedigitalbankingsystem.kafka.event.TransactionCompletedEvent;
import org.example.enterprisedigitalbankingsystem.notification.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionEventConsumerTest {

    @Mock
    private ProcessedEventRepository processedEventRepository;

    @Mock
    private NotificationService notificationService;

    private ObjectMapper objectMapper;
    private TransactionEventConsumer transactionEventConsumer;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        transactionEventConsumer = new TransactionEventConsumer(
                objectMapper,
                processedEventRepository,
                notificationService
        );
    }

    @Test
    @DisplayName("Should process transaction completed event and dispatch notification on first arrival")
    void shouldProcessEventAndDispatchNotificationOnFirstArrival() throws Exception {
        TransactionCompletedEvent event = TransactionCompletedEvent.builder()
                .eventId("evt-tx-100")
                .transactionId(1L)
                .transactionReference("TXN001")
                .transactionType("TRANSFER")
                .accountId(10L)
                .accountNumber("ACC-100")
                .customerId(5L)
                .userId("usr-1")
                .email("user@bank.com")
                .amount(new BigDecimal("150.00"))
                .balanceAfterTransaction(new BigDecimal("850.00"))
                .occurredAt(LocalDateTime.now())
                .build();

        String payload = objectMapper.writeValueAsString(event);
        when(processedEventRepository.existsByEventId("evt-tx-100")).thenReturn(false);

        transactionEventConsumer.consumeTransactionCompleted(payload, "transaction.completed", 0, 100L);

        // Verify async notification dispatched
        verify(notificationService, times(1)).sendTransactionNotification(any(TransactionCompletedEvent.class));

        // Verify event persisted in deduplication table
        ArgumentCaptor<ProcessedEvent> captor = ArgumentCaptor.forClass(ProcessedEvent.class);
        verify(processedEventRepository, times(1)).save(captor.capture());

        ProcessedEvent saved = captor.getValue();
        assertThat(saved.getEventId()).isEqualTo("evt-tx-100");
        assertThat(saved.getEventType()).isEqualTo("TRANSACTION_COMPLETED");
        assertThat(saved.getProcessedAt()).isNotNull();
    }

    @Test
    @DisplayName("Should detect duplicate event and skip notification and saving (Idempotency Guard)")
    void shouldDetectDuplicateAndSkipProcessing() throws Exception {
        TransactionCompletedEvent event = TransactionCompletedEvent.builder()
                .eventId("evt-tx-duplicate")
                .transactionId(2L)
                .transactionReference("TXN002")
                .transactionType("DEPOSIT")
                .accountId(11L)
                .accountNumber("ACC-101")
                .customerId(6L)
                .email("user2@bank.com")
                .amount(new BigDecimal("50.00"))
                .balanceAfterTransaction(new BigDecimal("550.00"))
                .occurredAt(LocalDateTime.now())
                .build();

        String payload = objectMapper.writeValueAsString(event);
        when(processedEventRepository.existsByEventId("evt-tx-duplicate")).thenReturn(true);

        transactionEventConsumer.consumeTransactionCompleted(payload, "transaction.completed", 0, 101L);

        // Verify notification is NOT dispatched
        verify(notificationService, never()).sendTransactionNotification(any());

        // Verify duplicate is NOT re-saved
        verify(processedEventRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should re-throw runtime exception on poison pill message so retry and DLT take over")
    void shouldReThrowExceptionOnPoisonPill() {
        String poisonPill = "{invalid-json-payload-broken";

        assertThatThrownBy(() ->
                transactionEventConsumer.consumeTransactionCompleted(poisonPill, "transaction.completed", 0, 102L))
                .isInstanceOf(RuntimeException.class);

        verify(notificationService, never()).sendTransactionNotification(any());
        verify(processedEventRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should handle DLT dead letter message safely without throwing exception")
    void shouldHandleDeadLetterSafely() {
        assertDoesNotThrow(() ->
                transactionEventConsumer.handleDeadLetter(
                        "{\"raw\":\"payload\"}",
                        "transaction.completed.DLT",
                        "Exhausted retries"
                )
        );
    }
}

