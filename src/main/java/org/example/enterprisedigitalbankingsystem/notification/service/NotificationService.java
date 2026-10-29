package org.example.enterprisedigitalbankingsystem.notification.service;

import org.example.enterprisedigitalbankingsystem.kafka.event.BeneficiaryActivatedEvent;
import org.example.enterprisedigitalbankingsystem.kafka.event.TransactionCompletedEvent;

/**
 * Day 61: Async Notification Service interface.
 * Dispatches notifications (Email/SMS/Push) triggered asynchronously by Kafka event consumers.
 */
public interface NotificationService {

    void sendTransactionNotification(TransactionCompletedEvent event);

    void sendBeneficiaryActivatedNotification(BeneficiaryActivatedEvent event);
}

