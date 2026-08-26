package org.example.enterprisedigitalbankingsystem.ledger.repository;

import org.example.enterprisedigitalbankingsystem.ledger.entity.LedgerEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LedgerRepository extends JpaRepository<LedgerEntry, Long> {
    List<LedgerEntry> findByAccountIdOrderByCreatedAtDesc(Long accountId);
    List<LedgerEntry> findByTransactionId(Long transactionId);

}
