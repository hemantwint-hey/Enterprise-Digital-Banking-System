package org.example.enterprisedigitalbankingsystem.loan.dto.response;


import lombok.*;
import org.example.enterprisedigitalbankingsystem.loan.entity.LoanStatus;
import org.example.enterprisedigitalbankingsystem.loan.entity.LoanType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanResponse {
    private Long id;
    private Long  customerId;
    private String disbursementAccountNumber;
    private LoanType loanType;
    private BigDecimal principalAmount;
    private BigDecimal interestRate;
    private Integer tenureMonths;
    private BigDecimal emiAmount;
    private LoanStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime approvedAt;
    private LocalDateTime closedAt;
}
