package org.example.enterprisedigitalbankingsystem.loan.repository;

import org.example.enterprisedigitalbankingsystem.loan.entity.Loan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LoanRepository extends JpaRepository<Loan, Long> {
    List<Loan> findByCustomerId(Long customerId);
}
