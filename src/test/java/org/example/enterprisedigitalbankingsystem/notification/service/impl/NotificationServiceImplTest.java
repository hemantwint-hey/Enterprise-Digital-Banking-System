package org.example.enterprisedigitalbankingsystem.notification.service.impl;

import org.example.enterprisedigitalbankingsystem.kafka.event.BeneficiaryActivatedEvent;
import org.example.enterprisedigitalbankingsystem.kafka.event.TransactionCompletedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class NotificationServiceImplTest {

    private NotificationServiceImpl notificationService;

    @BeforeEach
    void setUp() {
        notificationService = new NotificationServiceImpl();
    }

    @Test
    @DisplayName("Should send transaction notification successfully with complete event details")
    void shouldSendTransactionNotificationSuccessfully() {
        TransactionCompletedEvent event = TransactionCompletedEvent.builder()
                .eventId("evt-tx-001")
                .transactionId(101L)
                .transactionReference("TXN0000000000001")
                .transactionType("TRANSFER")
                .accountId(1L)
                .accountNumber("ACC-1001")
                .customerId(50L)
                .userId("usr-123")
                .email("alice@bank.com")
                .amount(new BigDecimal("250.00"))
                .balanceAfterTransaction(new BigDecimal("1750.00"))
                .occurredAt(LocalDateTime.now())
                .build();

        assertDoesNotThrow(() -> notificationService.sendTransactionNotification(event));
    }

    @Test
    @DisplayName("Should handle missing email in transaction notification gracefully")
    void shouldHandleMissingEmailInTransactionNotification() {
        TransactionCompletedEvent event = TransactionCompletedEvent.builder()
                .eventId("evt-tx-002")
                .transactionId(102L)
                .transactionReference("TXN0000000000002")
                .transactionType("DEPOSIT")
                .accountId(2L)
                .accountNumber("ACC-1002")
                .customerId(51L)
                .email(null)
                .amount(new BigDecimal("500.00"))
                .balanceAfterTransaction(new BigDecimal("500.00"))
                .occurredAt(LocalDateTime.now())
                .build();

        assertDoesNotThrow(() -> notificationService.sendTransactionNotification(event));
    }

    @Test
    @DisplayName("Should send beneficiary activation notification successfully")
    void shouldSendBeneficiaryActivatedNotificationSuccessfully() {
        BeneficiaryActivatedEvent event = BeneficiaryActivatedEvent.builder()
                .eventId("evt-ben-001")
                .beneficiaryId(201L)
                .customerId(50L)
                .userId("usr-123")
                .email("alice@bank.com")
                .nickName("Mom")
                .accountNumber("ACC-9999")
                .occurredAt(LocalDateTime.now())
                .build();

        assertDoesNotThrow(() -> notificationService.sendBeneficiaryActivatedNotification(event));
    }

    @Test
    @DisplayName("Should handle missing email in beneficiary notification gracefully")
    void shouldHandleMissingEmailInBeneficiaryNotification() {
        BeneficiaryActivatedEvent event = BeneficiaryActivatedEvent.builder()
                .eventId("evt-ben-002")
                .beneficiaryId(202L)
                .customerId(52L)
                .email("")
                .nickName("Vendor")
                .accountNumber("ACC-8888")
                .occurredAt(LocalDateTime.now())
                .build();

        assertDoesNotThrow(() -> notificationService.sendBeneficiaryActivatedNotification(event));
    }
}

