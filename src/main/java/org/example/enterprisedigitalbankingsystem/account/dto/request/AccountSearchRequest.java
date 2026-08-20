package org.example.enterprisedigitalbankingsystem.account.dto.request;

import jakarta.validation.constraints.DecimalMin;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.enterprisedigitalbankingsystem.account.entity.AccountStatus;
import org.example.enterprisedigitalbankingsystem.account.entity.AccountType;

import java.math.BigDecimal;

@NoArgsConstructor
@Data
@AllArgsConstructor
@Builder
public class AccountSearchRequest {
    private Long customerId;
    private String accountNumber;
    private AccountType accountType;
    private AccountStatus accountStatus;
    private String branch;

    @DecimalMin(value = "0.00", message = "Minimum balance cannot be negative")
    private BigDecimal minBalance;

    @DecimalMin(value = "0.00", message = "Maximum balance cannot be negative")
    private BigDecimal maxBalance;
}
