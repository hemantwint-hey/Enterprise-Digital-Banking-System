package org.example.enterprisedigitalbankingsystem.emi.service;

import org.example.enterprisedigitalbankingsystem.common.dto.PagedResponse;
import org.example.enterprisedigitalbankingsystem.emi.dto.response.EmiResponse;
import org.example.enterprisedigitalbankingsystem.loan.entity.Loan;
import org.springframework.data.domain.Pageable;

public interface EmiService {
    void generateSchedule(Loan loan);
    PagedResponse<EmiResponse> getEmisByLoanId(Long loanId, Pageable pageable);
    EmiResponse payEmi(Long emiId);
}
