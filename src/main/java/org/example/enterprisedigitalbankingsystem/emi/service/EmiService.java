package org.example.enterprisedigitalbankingsystem.emi.service;

import org.example.enterprisedigitalbankingsystem.emi.dto.response.EmiResponse;
import org.example.enterprisedigitalbankingsystem.loan.entity.Loan;

import java.util.List;

public interface EmiService {
    void generateSchedule(Loan loan);
    List<EmiResponse> getEmisByLoanId(Long loanId);
    EmiResponse payEmi(Long emiId);
}
