package org.example.enterprisedigitalbankingsystem.ledger.service.impl;

import org.example.enterprisedigitalbankingsystem.common.dto.PagedResponse;
import org.example.enterprisedigitalbankingsystem.common.util.SortValidator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import java.util.Set;

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

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "id", "createdAt", "amount", "entryType", "balanceAfterEntry"
    );
    private static final Sort DEFAULT_SORT = Sort.by(Sort.Direction.DESC, "createdAt");

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<LedgerEntryResponse> getLedgerByAccount(Long accountId, Pageable pageable) {
        Pageable validatedPageable = SortValidator.validate(pageable, ALLOWED_SORT_FIELDS, DEFAULT_SORT);
        Page<LedgerEntryResponse> page = ledgerRepository.findByAccountId(accountId, validatedPageable)
                .map(ledgerMapper::toResponse);
        return PagedResponse.from(page);
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
