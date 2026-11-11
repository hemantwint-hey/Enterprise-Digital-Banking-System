package org.example.enterprisedigitalbankingsystem.loan.service;

import org.example.enterprisedigitalbankingsystem.common.dto.PagedResponse;
import org.example.enterprisedigitalbankingsystem.loan.dto.request.CreateLoanRequest;
import org.example.enterprisedigitalbankingsystem.loan.dto.response.LoanResponse;
import org.springframework.data.domain.Pageable;

public interface LoanService {
    LoanResponse applyForLoan(CreateLoanRequest request);
    LoanResponse getLoanById(Long loanId);
    PagedResponse<LoanResponse> getLoansByCustomerId(Long customerId, Pageable pageable);
    LoanResponse approveLoan(Long loanId);
    LoanResponse rejectLoan(Long loanId);
}
