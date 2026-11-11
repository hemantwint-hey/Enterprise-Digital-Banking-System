package org.example.enterprisedigitalbankingsystem.transaction.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.enterprisedigitalbankingsystem.transaction.entity.TransactionStatus;
import org.example.enterprisedigitalbankingsystem.transaction.entity.TransactionType;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionSummaryResponse implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private Long transactionId;
    private String transactionReference;
    private BigDecimal amount;
    private TransactionType transactionType;
    private TransactionStatus transactionStatus;
    private LocalDateTime createdAt;
}
