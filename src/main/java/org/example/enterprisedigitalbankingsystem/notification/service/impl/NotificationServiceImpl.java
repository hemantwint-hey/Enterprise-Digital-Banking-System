package org.example.enterprisedigitalbankingsystem.notification.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.example.enterprisedigitalbankingsystem.kafka.event.BeneficiaryActivatedEvent;
import org.example.enterprisedigitalbankingsystem.kafka.event.TransactionCompletedEvent;
import org.example.enterprisedigitalbankingsystem.notification.service.NotificationService;
import org.springframework.stereotype.Service;

/**
 * Day 61: Async Notification Service implementation.
 * Formats customer alerts and dispatches async notifications upon receiving verified Kafka events.
 */
@Service
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    @Override
    public void sendTransactionNotification(TransactionCompletedEvent event) {
        String recipient = (event.getEmail() != null && !event.getEmail().isBlank()) ? event.getEmail() : "Default Channel";
        log.info("[NOTIFICATION DISPATCHED] Type=TRANSACTION_ALERT, CustomerId={}, Account={}, Action={}, Amount={}, Balance={}, Recipient={}",
                event.getCustomerId(),
                event.getAccountNumber(),
                event.getTransactionType(),
                event.getAmount(),
                event.getBalanceAfterTransaction(),
                recipient);
    }

    @Override
    public void sendBeneficiaryActivatedNotification(BeneficiaryActivatedEvent event) {
        String recipient = (event.getEmail() != null && !event.getEmail().isBlank()) ? event.getEmail() : "Default Channel";
        log.info("[NOTIFICATION DISPATCHED] Type=BENEFICIARY_ACTIVATION, CustomerId={}, BeneficiaryId={}, Nickname='{}', Account={}, Recipient={}",
                event.getCustomerId(),
                event.getBeneficiaryId(),
                event.getNickName(),
                event.getAccountNumber(),
                recipient);
    }
}

