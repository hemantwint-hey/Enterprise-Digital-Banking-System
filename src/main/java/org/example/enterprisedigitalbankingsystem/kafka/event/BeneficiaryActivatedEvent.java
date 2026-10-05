package org.example.enterprisedigitalbankingsystem.kafka.event;

import java.time.LocalDateTime;

public class BeneficiaryActivatedEvent {
    private String eventId;
    private Long beneficiaryId;
    private Long customerId;
    private Long userId;
    private String email;
    private String nickName;
    private String accountName;
    private LocalDateTime occuredAt;
}
