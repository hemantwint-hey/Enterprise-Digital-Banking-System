package org.example.enterprisedigitalbankingsystem.transaction.repository;

import org.example.enterprisedigitalbankingsystem.transaction.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TransactionRepository  extends JpaRepository<Transaction,Long> {
    Optional<Transaction> fiindByTransactionReference(String transactionReference);
    boolean existsByTransactionReference(String transactionReference);
    List<Transaction> findBySourceAccountIdOrDestinationAccountIdOrderByCreatedAtDesc(
            Long sourceAccountId,
            Long destinationAccountId
    );
}
