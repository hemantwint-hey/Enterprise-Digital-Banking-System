package org.example.enterprisedigitalbankingsystem.account.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.enterprisedigitalbankingsystem.account.entity.AccountStatus;
import org.example.enterprisedigitalbankingsystem.account.entity.AccountType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AccountResponse{
    private long accountId;
    private String accountNumber;
    private Long customerId;
    private Long customerName;
    private String accountHolderName;
    private String branch;
    private BigDecimal balance;
    private AccountType accountType;
    private AccountStatus accountStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
