package org.example.enterprisedigitalbankingsystem.ledger.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.enterprisedigitalbankingsystem.account.entity.Account;
import org.example.enterprisedigitalbankingsystem.exception.ResourceNotFoundException;
import org.example.enterprisedigitalbankingsystem.ledger.dto.response.LedgerEntryResponse;
import org.example.enterprisedigitalbankingsystem.ledger.entity.EntryType;
import org.example.enterprisedigitalbankingsystem.ledger.entity.LedgerEntry;
import org.example.enterprisedigitalbankingsystem.ledger.mapper.LedgerMapper;
import org.example.enterprisedigitalbankingsystem.ledger.repository.LedgerRepository;
import org.example.enterprisedigitalbankingsystem.ledger.service.LedgerService;
import org.example.enterprisedigitalbankingsystem.transaction.entity.Transaction;
import org.example.enterprisedigitalbankingsystem.transaction.entity.TransactionType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class LedgerServiceImpl implements LedgerService {

    private final LedgerRepository ledgerRepository;
    private final LedgerMapper ledgerMapper;

    @Override
    public LedgerEntry recordEntry(Transaction transaction, Account account, EntryType entryType,
                                    BigDecimal amount, BigDecimal balanceAfterEntry, String narration) {

        LedgerEntry ledgerEntry = LedgerEntry.builder()
                .transaction(transaction)
                .account(account)
                .entryType(entryType)
                .amount(amount)
                .balanceAfterEntry(balanceAfterEntry)
                .narration(narration)
                .build();

        return ledgerRepository.save(ledgerEntry);
    }

    @Override
    public void recordTransactionEntries(Transaction transaction) {

        String narration = transaction.getRemarks() != null
                ? transaction.getRemarks()
                : transaction.getTransactionType() + " " + transaction.getTransactionReference();

        TransactionType type = transaction.getTransactionType();

        if (type == TransactionType.DEPOSIT) {
            Account destination = transaction.getDestinationAccount();
            recordEntry(transaction, destination, EntryType.CREDIT,
                    transaction.getAmount(), destination.getBalance(), narration);

        } else if (type == TransactionType.WITHDRAWAL) {
            Account source = transaction.getSourceAccount();
            recordEntry(transaction, source, EntryType.DEBIT,
                    transaction.getAmount(), source.getBalance(), narration);

        } else if (type == TransactionType.TRANSFER) {
            Account source = transaction.getSourceAccount();
            Account destination = transaction.getDestinationAccount();

            recordEntry(transaction, source, EntryType.DEBIT,
                    transaction.getAmount(), source.getBalance(), narration);
            recordEntry(transaction, destination, EntryType.CREDIT,
                    transaction.getAmount(), destination.getBalance(), narration);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<LedgerEntryResponse> getLedgerByAccount(Long accountId) {
        List<LedgerEntry> entries = ledgerRepository.findByAccountIdOrderByCreatedAtDesc(accountId);
        return entries.stream().map(ledgerMapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<LedgerEntryResponse> getLedgerByTransaction(Long transactionId) {
        List<LedgerEntry> entries = ledgerRepository.findByTransactionId(transactionId);
        if (entries.isEmpty()) {
            throw new ResourceNotFoundException("No ledger entries found for transaction id: " + transactionId);
        }
        return entries.stream().map(ledgerMapper::toResponse).toList();
    }
}
