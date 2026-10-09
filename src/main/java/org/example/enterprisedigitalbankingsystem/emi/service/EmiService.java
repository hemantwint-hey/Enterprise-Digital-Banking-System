package org.example.enterprisedigitalbankingsystem.emi.service;

import org.example.enterprisedigitalbankingsystem.loan.entity.Loan;

public interface EmiService {
    void generateSchedule(Loan loan);
}
