package org.example.enterprisedigitalbankingsystem.loan.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;
import org.example.enterprisedigitalbankingsystem.loan.entity.LoanType;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateLoanRequest {

    @NotNull(message = "Customer id is required")
    @Positive(message = "Customer id must be positive")
    private Long customerId;

    @NotNull(message = "Disbursement account id is required")
    @Positive(message = "Disbursement account id must be positive")
    private Long disbursementAccountId;

    @NotNull(message = "Loan type is required")
    private LoanType loanType;

    @NotNull(message = "Principal amount is required")
    @DecimalMin(value = "1000.00", message = "Principal must be at least 1000.00")
    @DecimalMax(value = "100000000.00", message = "Principal amount cannot exceed 100,000,000.00")
    @Digits(integer = 16, fraction = 2, message = "Principal amount must have at most 2 decimal places")
    private BigDecimal principalAmount;

    @NotNull(message = "Interest rate is required")
    @DecimalMin(value = "0.01", message = "Interest rate must be positive")
    @DecimalMax(value = "40.00", message = "Interest rate cannot exceed 40.00%")
    @Digits(integer = 3, fraction = 2, message = "Interest rate format invalid (max 2 decimal places)")
    private BigDecimal interestRate;

    @NotNull(message = "Tenure is required")
    @Min(value = 1, message = "Tenure must be at least 1 month")
    @Max(value = 360, message = "Tenure cannot exceed 360 months")
    private Integer tenureMonths;
}
