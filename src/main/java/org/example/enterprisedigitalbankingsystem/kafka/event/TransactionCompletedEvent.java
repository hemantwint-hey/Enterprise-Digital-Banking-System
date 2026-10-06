package org.example.enterprisedigitalbankingsystem.kafka.event;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionCompletedEvent {
    private String eventId;
    private Long transactionId;
    private String transactionReference;
    private String transactionType;
    private Long accountId;
    private String accountNumber;
    private Long customerId;
    private String userId;
    private String email;
    private BigDecimal amount;
    private BigDecimal balanceAfterTransaction;
    private LocalDateTime occurredAt;
}
