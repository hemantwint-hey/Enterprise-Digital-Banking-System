package org.example.enterprisedigitalbankingsystem.ledger.service;

import org.example.enterprisedigitalbankingsystem.account.entity.Account;
import org.example.enterprisedigitalbankingsystem.ledger.dto.response.LedgerEntryResponse;
import org.example.enterprisedigitalbankingsystem.ledger.entity.EntryType;
import org.example.enterprisedigitalbankingsystem.ledger.entity.LedgerEntry;
import org.example.enterprisedigitalbankingsystem.transaction.entity.Transaction;

import java.math.BigDecimal;
import java.util.List;

public interface LedgerService {
    LedgerEntry recordEntry(Transaction transaction, Account account, EntryType entryType,
                             BigDecimal amount, BigDecimal balanceAfterEntry, String narration);
    void recordTransactionEntries(Transaction transaction);
    List<LedgerEntryResponse> getLedgerByAccount(Long accountId);
    List<LedgerEntryResponse> getLedgerByTransaction(Long transactionId);
}
