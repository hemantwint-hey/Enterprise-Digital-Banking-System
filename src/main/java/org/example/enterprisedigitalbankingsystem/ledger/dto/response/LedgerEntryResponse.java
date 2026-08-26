package org.example.enterprisedigitalbankingsystem.ledger.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.enterprisedigitalbankingsystem.ledger.entity.EntryType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LedgerEntryResponse {
    private Long id;
    private Long transactionId;
    private Long accountId;
    private String accountNumber;
    private EntryType entryType;
    private BigDecimal amount;
    private BigDecimal balanceAfterEntry;
    private String narration;
    private LocalDateTime createdAt;
}
