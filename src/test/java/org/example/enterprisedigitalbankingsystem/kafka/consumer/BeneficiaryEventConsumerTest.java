package org.example.enterprisedigitalbankingsystem.kafka.consumer;

import org.example.enterprisedigitalbankingsystem.kafka.consumer.entity.ProcessedEvent;
import org.example.enterprisedigitalbankingsystem.kafka.consumer.repository.ProcessedEventRepository;
import org.example.enterprisedigitalbankingsystem.kafka.event.BeneficiaryActivatedEvent;
import org.example.enterprisedigitalbankingsystem.notification.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BeneficiaryEventConsumerTest {

    @Mock
    private ProcessedEventRepository processedEventRepository;

    @Mock
    private NotificationService notificationService;

    private ObjectMapper objectMapper;
    private BeneficiaryEventConsumer beneficiaryEventConsumer;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        beneficiaryEventConsumer = new BeneficiaryEventConsumer(
                objectMapper,
                processedEventRepository,
                notificationService
        );
    }

    @Test
    @DisplayName("Should process beneficiary activated event and dispatch notification on first arrival")
    void shouldProcessBeneficiaryActivatedEventAndDispatchNotification() throws Exception {
        BeneficiaryActivatedEvent event = BeneficiaryActivatedEvent.builder()
                .eventId("evt-ben-100")
                .beneficiaryId(30L)
                .customerId(10L)
                .userId("usr-10")
                .email("alice@bank.com")
                .nickName("Landlord")
                .accountNumber("ACC-7777")
                .occurredAt(LocalDateTime.now())
                .build();

        String payload = objectMapper.writeValueAsString(event);
        when(processedEventRepository.existsByEventId("evt-ben-100")).thenReturn(false);

        beneficiaryEventConsumer.consumeBeneficiaryActivated(payload, "beneficiary.activated", 0, 200L);

        // Verify async notification dispatched
        verify(notificationService, times(1)).sendBeneficiaryActivatedNotification(any(BeneficiaryActivatedEvent.class));

        // Verify deduplication entry saved
        ArgumentCaptor<ProcessedEvent> captor = ArgumentCaptor.forClass(ProcessedEvent.class);
        verify(processedEventRepository, times(1)).save(captor.capture());

        ProcessedEvent saved = captor.getValue();
        assertThat(saved.getEventId()).isEqualTo("evt-ben-100");
        assertThat(saved.getEventType()).isEqualTo("BENEFICIARY_ACTIVATED");
        assertThat(saved.getProcessedAt()).isNotNull();
    }

    @Test
    @DisplayName("Should detect duplicate beneficiary event and skip processing (Idempotency Guard)")
    void shouldDetectDuplicateBeneficiaryEventAndSkip() throws Exception {
        BeneficiaryActivatedEvent event = BeneficiaryActivatedEvent.builder()
                .eventId("evt-ben-duplicate")
                .beneficiaryId(31L)
                .customerId(11L)
                .nickName("Dentist")
                .accountNumber("ACC-8888")
                .occurredAt(LocalDateTime.now())
                .build();

        String payload = objectMapper.writeValueAsString(event);
        when(processedEventRepository.existsByEventId("evt-ben-duplicate")).thenReturn(true);

        beneficiaryEventConsumer.consumeBeneficiaryActivated(payload, "beneficiary.activated", 0, 201L);

        // Verify notification is NOT dispatched
        verify(notificationService, never()).sendBeneficiaryActivatedNotification(any());

        // Verify record is NOT re-saved
        verify(processedEventRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should re-throw exception on poison pill message so retry and DLT take over")
    void shouldReThrowExceptionOnPoisonPill() {
        String invalidPayload = "{malformed-payload-missing-braces";

        assertThatThrownBy(() ->
                beneficiaryEventConsumer.consumeBeneficiaryActivated(invalidPayload, "beneficiary.activated", 0, 202L))
                .isInstanceOf(RuntimeException.class);

        verify(notificationService, never()).sendBeneficiaryActivatedNotification(any());
        verify(processedEventRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should handle DLT dead letter message safely without throwing exception")
    void shouldHandleDeadLetterSafely() {
        assertDoesNotThrow(() ->
                beneficiaryEventConsumer.handleDeadLetter(
                        "{\"raw\":\"beneficiary-failed\"}",
                        "beneficiary.activated.DLT",
                        "Connection timed out"
                )
        );
    }
}

