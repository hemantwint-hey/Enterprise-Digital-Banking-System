package org.example.enterprisedigitalbankingsystem.loan.mapper;

import org.example.enterprisedigitalbankingsystem.loan.dto.response.LoanResponse;
import org.example.enterprisedigitalbankingsystem.loan.entity.Loan;
import org.springframework.stereotype.Component;

@Component
public class LoanMapper {
    public LoanResponse toResponse(Loan loan){
        if(loan == null)return null;

        return LoanResponse.builder()
                .id(loan.getId())
                .customerId(loan.getCustomer().getId())
                .disbursementAccountNumber(loan.getDisbursementAccount().getAccountNumber())
                .loanType(loan.getLoanType())
                .principalAmount(loan.getPrincipalAmount())
                .interestRate(loan.getInterestRate())
                .tenureMonths(loan.getTenureMonths())
                .emiAmount(loan.getEmiAmount())
                .createdAt(loan.getCreatedAt())
                .approvedAt(loan.getApprovedAt())
                .closedAt(loan.getClosedAt())
                .build();
    }
}
