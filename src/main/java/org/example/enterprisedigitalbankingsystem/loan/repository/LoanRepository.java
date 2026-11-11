package org.example.enterprisedigitalbankingsystem.loan.repository;

import org.example.enterprisedigitalbankingsystem.loan.entity.Loan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LoanRepository extends JpaRepository<Loan, Long> {

    @EntityGraph(attributePaths = {"customer", "disbursementAccount"})
    @Override
    Optional<Loan> findById(Long id);

    @EntityGraph(attributePaths = {"customer", "disbursementAccount"})
    Page<Loan> findByCustomerId(Long customerId, Pageable pageable);
}
