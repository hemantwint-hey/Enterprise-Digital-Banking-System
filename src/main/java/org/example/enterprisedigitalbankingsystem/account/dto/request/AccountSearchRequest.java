package org.example.enterprisedigitalbankingsystem.account.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
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

    @Positive(message = "Customer id must be positive")
    private Long customerId;

    @Size(max = 30, message = "Account number cannot exceed 30 characters")
    private String accountNumber;

    private AccountType accountType;
    private AccountStatus accountStatus;

    @Size(max = 100, message = "Branch cannot exceed 100 characters")
    private String branch;

    @DecimalMin(value = "0.00", message = "Minimum balance cannot be negative")
    @Digits(integer = 16, fraction = 2, message = "Minimum balance supports up to 2 decimal places")
    private BigDecimal minBalance;

    @DecimalMin(value = "0.00", message = "Maximum balance cannot be negative")
    @Digits(integer = 16, fraction = 2, message = "Maximum balance supports up to 2 decimal places")
    private BigDecimal maxBalance;

    @JsonIgnore
    @AssertTrue(message = "minBalance cannot exceed maxBalance")
    public boolean isBalanceRangeValid() {
        return minBalance == null || maxBalance == null || minBalance.compareTo(maxBalance) <= 0;
    }
}
