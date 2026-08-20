package org.example.enterprisedigitalbankingsystem.account.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.enterprisedigitalbankingsystem.account.entity.AccountType;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateAccountRequest {
    @NotNull(message = "Customer id should not be null")
    private Long customerId;

    @NotNull(message = "Account type is required")
    private AccountType accountType;

    @NotBlank(message = "Branch is required")
    @Size(max = 100, message = "Branch cannot exceed 100 characters")
    private String branch;

    @NotNull(message = "Opening balance is required")
    @DecimalMin(value = "0.00", message = "Opening balance cannot be negative")
    private BigDecimal openingBalance;
}
