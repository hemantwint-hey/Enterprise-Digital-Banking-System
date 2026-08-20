package org.example.enterprisedigitalbankingsystem.account.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.enterprisedigitalbankingsystem.account.entity.AccountStatus;
import org.example.enterprisedigitalbankingsystem.account.entity.AccountType;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AccountSummaryResponse {
        private Long accountId;
        private String accountNumber;
        private String accountHolderName;
        private BigDecimal balance;
        private AccountType accountType;
        private AccountStatus accountStatus;
}
