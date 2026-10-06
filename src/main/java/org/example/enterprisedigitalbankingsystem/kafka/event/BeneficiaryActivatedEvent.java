package org.example.enterprisedigitalbankingsystem.kafka.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BeneficiaryActivatedEvent {
    private String eventId;
    private Long beneficiaryId;
    private Long customerId;
    private String userId;
    private String email;
    private String nickName;
    private String accountNumber;
    private LocalDateTime occurredAt;
}
