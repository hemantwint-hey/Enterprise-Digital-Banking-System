package org.example.enterprisedigitalbankingsystem.emi.repository;

import org.example.enterprisedigitalbankingsystem.emi.entity.Emi;
import org.example.enterprisedigitalbankingsystem.emi.entity.EmiStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EmiRepository extends JpaRepository<Emi, Long> {
    List<Emi> findByLoanIdOrderByInstallmentNumberAsc(Long loanId);
    boolean existsByLoanIdAndStatusNot(Long loanId, EmiStatus status);
}
