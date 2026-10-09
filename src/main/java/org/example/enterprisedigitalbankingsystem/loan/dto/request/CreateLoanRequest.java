package org.example.enterprisedigitalbankingsystem.loan.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.example.enterprisedigitalbankingsystem.loan.entity.LoanType;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateLoanRequest {
    @NotNull(message = "Ccustomer id is required")
    private Long customerId;
    @NotNull(message = "Disbursement account id is required")
    private Long disbursementAccountId;
    @NotNull(message = "Loan typeis required" )
    private LoanType loanType;

    @NotNull(message = "Principal amounnt is required")
    @DecimalMin(value = "1000.00", message = "Principal must be at least 1000")
    private BigDecimal principalAmount;

    @NotNull(message = "Interest rate is required")
    @DecimalMin(value = "0.01" , message = "Interest rate must be prositive")
    private BigDecimal interestRate;

    @NotNull(message = "Tenure is required")
    @Min(value = 1, message = "Tenure must be at least 1 month")
    @Max(value = 360 , message = "Tenure cannot exceed  360 months")
    private Integer tenureMonths;


}
