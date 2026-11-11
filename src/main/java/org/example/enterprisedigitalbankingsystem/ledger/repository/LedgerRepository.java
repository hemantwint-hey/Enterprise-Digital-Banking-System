package org.example.enterprisedigitalbankingsystem.ledger.repository;

import org.example.enterprisedigitalbankingsystem.ledger.entity.LedgerEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LedgerRepository extends JpaRepository<LedgerEntry, Long> {

    @EntityGraph(attributePaths = {"account", "transaction"})
    Page<LedgerEntry> findByAccountId(Long accountId, Pageable pageable);

    @EntityGraph(attributePaths = {"account", "transaction"})
    List<LedgerEntry> findByTransactionId(Long transactionId);
}
