package org.example.enterprisedigitalbankingsystem.ledger.service.impl;

import org.example.enterprisedigitalbankingsystem.account.entity.Account;
import org.example.enterprisedigitalbankingsystem.account.entity.AccountStatus;
import org.example.enterprisedigitalbankingsystem.account.entity.AccountType;
import org.example.enterprisedigitalbankingsystem.exception.ResourceNotFoundException;
import org.example.enterprisedigitalbankingsystem.ledger.dto.response.LedgerEntryResponse;
import org.example.enterprisedigitalbankingsystem.ledger.entity.EntryType;
import org.example.enterprisedigitalbankingsystem.ledger.entity.LedgerEntry;
import org.example.enterprisedigitalbankingsystem.ledger.mapper.LedgerMapper;
import org.example.enterprisedigitalbankingsystem.ledger.repository.LedgerRepository;
import org.example.enterprisedigitalbankingsystem.transaction.entity.Transaction;
import org.example.enterprisedigitalbankingsystem.transaction.entity.TransactionStatus;
import org.example.enterprisedigitalbankingsystem.transaction.entity.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Day 36 - Ledger Testing: verifies the double-entry accounting logic in
 * LedgerServiceImpl.recordTransactionEntries() for DEPOSIT, WITHDRAWAL and TRANSFER.
 */
@ExtendWith(MockitoExtension.class)
class LedgerServiceImplTest {

    @Mock
    private LedgerRepository ledgerRepository;

    private LedgerServiceImpl ledgerService;

    private Account sourceAccount;
    private Account destinationAccount;

    @BeforeEach
    void setUp() {
        ledgerService = new LedgerServiceImpl(ledgerRepository, new LedgerMapper());

        sourceAccount = Account.builder()
                .id(1L)
                .accountNumber("ACC-SOURCE-001")
                .accountHolderName("Alice")
                .branch("Main")
                .balance(new BigDecimal("500.00"))
                .accountType(AccountType.SAVINGS)
                .accountStatus(AccountStatus.ACTIVE)
                .build();

        destinationAccount = Account.builder()
                .id(2L)
                .accountNumber("ACC-DEST-002")
                .accountHolderName("Bob")
                .branch("Main")
                .balance(new BigDecimal("700.00"))
                .accountType(AccountType.SAVINGS)
                .accountStatus(AccountStatus.ACTIVE)
                .build();
    }

    private void stubSaveReturnsArgument() {
        when(ledgerRepository.save(any(LedgerEntry.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    private Transaction buildTransaction(TransactionType type, Account source, Account destination,
                                          BigDecimal amount, BigDecimal balanceAfter) {
        Transaction transaction = new Transaction();
        transaction.setId(100L);
        transaction.setTransactionReference("TXN0000000000001");
        transaction.setSourceAccount(source);
        transaction.setDestinationAccount(destination);
        transaction.setAmount(amount);
        transaction.setTransactionType(type);
        transaction.setTransactionStatus(TransactionStatus.SUCCESS);
        transaction.setBalanceAfterTransaction(balanceAfter);
        return transaction;
    }

    @Test
    void recordTransactionEntries_deposit_createsSingleCreditEntry() {
        stubSaveReturnsArgument();
        Transaction deposit = buildTransaction(TransactionType.DEPOSIT, null, destinationAccount,
                new BigDecimal("200.00"), destinationAccount.getBalance());

        ledgerService.recordTransactionEntries(deposit);

        ArgumentCaptor<LedgerEntry> captor = ArgumentCaptor.forClass(LedgerEntry.class);
        verify(ledgerRepository, times(1)).save(captor.capture());

        LedgerEntry entry = captor.getValue();
        assertThat(entry.getEntryType()).isEqualTo(EntryType.CREDIT);
        assertThat(entry.getAccount()).isEqualTo(destinationAccount);
        assertThat(entry.getAmount()).isEqualByComparingTo("200.00");
        assertThat(entry.getBalanceAfterEntry()).isEqualByComparingTo(destinationAccount.getBalance());
        assertThat(entry.getTransaction()).isEqualTo(deposit);
    }

    @Test
    void recordTransactionEntries_withdrawal_createsSingleDebitEntry() {
        stubSaveReturnsArgument();
        Transaction withdrawal = buildTransaction(TransactionType.WITHDRAWAL, sourceAccount, null,
                new BigDecimal("150.00"), sourceAccount.getBalance());

        ledgerService.recordTransactionEntries(withdrawal);

        ArgumentCaptor<LedgerEntry> captor = ArgumentCaptor.forClass(LedgerEntry.class);
        verify(ledgerRepository, times(1)).save(captor.capture());

        LedgerEntry entry = captor.getValue();
        assertThat(entry.getEntryType()).isEqualTo(EntryType.DEBIT);
        assertThat(entry.getAccount()).isEqualTo(sourceAccount);
        assertThat(entry.getAmount()).isEqualByComparingTo("150.00");
    }

    @Test
    void recordTransactionEntries_transfer_createsBalancedDebitAndCreditEntries() {
        stubSaveReturnsArgument();
        Transaction transfer = buildTransaction(TransactionType.TRANSFER, sourceAccount, destinationAccount,
                new BigDecimal("300.00"), sourceAccount.getBalance());

        ledgerService.recordTransactionEntries(transfer);

        ArgumentCaptor<LedgerEntry> captor = ArgumentCaptor.forClass(LedgerEntry.class);
        verify(ledgerRepository, times(2)).save(captor.capture());

        List<LedgerEntry> entries = captor.getAllValues();
        LedgerEntry debitEntry = entries.stream()
                .filter(e -> e.getEntryType() == EntryType.DEBIT)
                .findFirst().orElseThrow();
        LedgerEntry creditEntry = entries.stream()
                .filter(e -> e.getEntryType() == EntryType.CREDIT)
                .findFirst().orElseThrow();

        assertThat(debitEntry.getAccount()).isEqualTo(sourceAccount);
        assertThat(creditEntry.getAccount()).isEqualTo(destinationAccount);

        // Core double-entry rule: every debit must be matched by an equal credit.
        assertThat(debitEntry.getAmount()).isEqualByComparingTo(creditEntry.getAmount());
        assertThat(debitEntry.getAmount()).isEqualByComparingTo("300.00");

        // Both entries must reference the same originating transaction.
        assertThat(debitEntry.getTransaction()).isEqualTo(creditEntry.getTransaction()).isEqualTo(transfer);
    }

    @Test
    void getLedgerByAccount_returnsMappedResponses() {
        LedgerEntry entry = LedgerEntry.builder()
                .id(1L)
                .transaction(buildTransaction(TransactionType.DEPOSIT, null, destinationAccount,
                        new BigDecimal("50.00"), destinationAccount.getBalance()))
                .account(destinationAccount)
                .entryType(EntryType.CREDIT)
                .amount(new BigDecimal("50.00"))
                .balanceAfterEntry(destinationAccount.getBalance())
                .narration("Deposit")
                .build();

        when(ledgerRepository.findByAccountIdOrderByCreatedAtDesc(2L)).thenReturn(List.of(entry));

        List<LedgerEntryResponse> responses = ledgerService.getLedgerByAccount(2L);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getAccountId()).isEqualTo(2L);
        assertThat(responses.get(0).getEntryType()).isEqualTo(EntryType.CREDIT);
        assertThat(responses.get(0).getAmount()).isEqualByComparingTo("50.00");
    }

    @Test
    void getLedgerByTransaction_whenNoEntriesFound_throwsResourceNotFoundException() {
        when(ledgerRepository.findByTransactionId(999L)).thenReturn(List.of());

        assertThatThrownBy(() -> ledgerService.getLedgerByTransaction(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");
    }
}
