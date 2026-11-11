package org.example.enterprisedigitalbankingsystem.transaction.repository;

import org.example.enterprisedigitalbankingsystem.transaction.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long>, JpaSpecificationExecutor<Transaction> {

    @EntityGraph(attributePaths = {"sourceAccount", "destinationAccount"})
    Optional<Transaction> findByTransactionReference(String transactionReference);

    @EntityGraph(attributePaths = {"sourceAccount", "destinationAccount"})
    @Override
    Optional<Transaction> findById(Long id);

    boolean existsByTransactionReference(String transactionReference);

    Page<Transaction> findBySourceAccountIdOrDestinationAccountId(
            Long sourceAccountId,
            Long destinationAccountId,
            Pageable pageable
    );
}
