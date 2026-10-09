package org.example.enterprisedigitalbankingsystem.loan.service;

import org.example.enterprisedigitalbankingsystem.loan.dto.request.CreateLoanRequest;
import org.example.enterprisedigitalbankingsystem.loan.dto.response.LoanResponse;
import org.example.enterprisedigitalbankingsystem.loan.entity.Loan;

import java.util.List;

public interface LoanService {
    LoanResponse applyForLoan(CreateLoanRequest request);
    LoanResponse getLoanById(Long loanId);
    List<LoanResponse> getLoansByCustomerId(Long customerId);
    LoanResponse approveLoan(Long loanId);
    LoanResponse rejectLoan(Long loanId);
}
