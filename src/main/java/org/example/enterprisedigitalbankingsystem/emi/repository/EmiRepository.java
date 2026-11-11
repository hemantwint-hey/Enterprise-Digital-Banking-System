package org.example.enterprisedigitalbankingsystem.emi.repository;

import org.example.enterprisedigitalbankingsystem.emi.entity.Emi;
import org.example.enterprisedigitalbankingsystem.emi.entity.EmiStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmiRepository extends JpaRepository<Emi, Long> {

    Page<Emi> findByLoanId(Long loanId, Pageable pageable);

    boolean existsByLoanIdAndStatusNot(Long loanId, EmiStatus status);
}
