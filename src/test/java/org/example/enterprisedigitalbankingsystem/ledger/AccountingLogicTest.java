package org.example.enterprisedigitalbankingsystem.ledger;

import org.example.enterprisedigitalbankingsystem.account.entity.Account;
import org.example.enterprisedigitalbankingsystem.account.entity.AccountStatus;
import org.example.enterprisedigitalbankingsystem.account.entity.AccountType;
import org.example.enterprisedigitalbankingsystem.account.repository.AccountRepository;
import org.example.enterprisedigitalbankingsystem.ledger.entity.EntryType;
import org.example.enterprisedigitalbankingsystem.ledger.entity.LedgerEntry;
import org.example.enterprisedigitalbankingsystem.ledger.mapper.LedgerMapper;
import org.example.enterprisedigitalbankingsystem.ledger.repository.LedgerRepository;
import org.example.enterprisedigitalbankingsystem.ledger.service.impl.LedgerServiceImpl;
import org.example.enterprisedigitalbankingsystem.transaction.dto.request.DepositRequest;
import org.example.enterprisedigitalbankingsystem.transaction.dto.request.TransferRequest;
import org.example.enterprisedigitalbankingsystem.transaction.dto.request.WithdrawRequest;
import org.example.enterprisedigitalbankingsystem.transaction.entity.Transaction;
import org.example.enterprisedigitalbankingsystem.transaction.mapper.TransactionMapper;
import org.example.enterprisedigitalbankingsystem.transaction.repository.TransactionRepository;
import org.example.enterprisedigitalbankingsystem.transaction.service.impl.TransactionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Day 36 - Ledger Testing deliverable: "Verify accounting logic".
 *
 * Wires the REAL TransactionServiceImpl to the REAL LedgerServiceImpl (only the
 * JPA repositories are mocked) so the full deposit/withdraw/transfer -> ledger
 * pipeline runs exactly as it would against a database, and asserts the
 * fundamental double-entry rule: for every transaction, total debits == total
 * credits, and every ledger entry is traceable back to its transaction.
 */
@ExtendWith(MockitoExtension.class)
class AccountingLogicTest {

    @Mock
    private AccountRepository accountRepository;
    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private LedgerRepository ledgerRepository;

    private TransactionServiceImpl transactionService;

    @BeforeEach
    void setUp() {
        LedgerServiceImpl ledgerService = new LedgerServiceImpl(ledgerRepository, new LedgerMapper());
        transactionService = new TransactionServiceImpl(
                transactionRepository, accountRepository, new TransactionMapper(), ledgerService);

        when(transactionRepository.existsByTransactionReference(anyString())).thenReturn(false);
        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(ledgerRepository.save(any(LedgerEntry.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    private Account account(long id, String number, String balance) {
        return Account.builder()
                .id(id)
                .accountNumber(number)
                .accountHolderName("Holder-" + id)
                .branch("Main")
                .balance(new BigDecimal(balance))
                .accountType(AccountType.SAVINGS)
                .accountStatus(AccountStatus.ACTIVE)
                .build();
    }

    @Test
    void transfer_producesBalancedDoubleEntry_debitsEqualCredits() {
        Account source = account(1L, "ACC-001", "1000.00");
        Account destination = account(2L, "ACC-002", "200.00");
        when(accountRepository.findById(1L)).thenReturn(Optional.of(source));
        when(accountRepository.findById(2L)).thenReturn(Optional.of(destination));

        transactionService.transfer(TransferRequest.builder()
                .sourceAccountId(1L)
                .destinationAccountId(2L)
                .amount(new BigDecimal("400.00"))
                .build());

        ArgumentCaptor<LedgerEntry> captor = ArgumentCaptor.forClass(LedgerEntry.class);
        org.mockito.Mockito.verify(ledgerRepository, org.mockito.Mockito.times(2)).save(captor.capture());
        List<LedgerEntry> entries = captor.getAllValues();

        BigDecimal totalDebits = entries.stream()
                .filter(e -> e.getEntryType() == EntryType.DEBIT)
                .map(LedgerEntry::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalCredits = entries.stream()
                .filter(e -> e.getEntryType() == EntryType.CREDIT)
                .map(LedgerEntry::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // The core accounting invariant: debits must equal credits for every transaction.
        assertThat(totalDebits).isEqualByComparingTo(totalCredits);
        assertThat(totalDebits).isEqualByComparingTo("400.00");

        // Post-transfer balances reconcile with the ledger.
        assertThat(source.getBalance()).isEqualByComparingTo("600.00");
        assertThat(destination.getBalance()).isEqualByComparingTo("600.00");

        // Every entry traces back to the same transaction.
        assertThat(entries).allSatisfy(e -> assertThat(e.getTransaction().getTransactionReference())
                .isEqualTo(entries.get(0).getTransaction().getTransactionReference()));
    }

    @Test
    void deposit_thenWithdrawal_netLedgerMovementMatchesFinalBalance() {
        Account acc = account(3L, "ACC-003", "0.00");
        when(accountRepository.findById(3L)).thenReturn(Optional.of(acc));

        transactionService.deposit(DepositRequest.builder()
                .accountId(3L).amount(new BigDecimal("1000.00")).build());
        transactionService.withdraw(WithdrawRequest.builder()
                .accountId(3L).amount(new BigDecimal("350.00")).build());

        ArgumentCaptor<LedgerEntry> captor = ArgumentCaptor.forClass(LedgerEntry.class);
        org.mockito.Mockito.verify(ledgerRepository, org.mockito.Mockito.times(2)).save(captor.capture());
        List<LedgerEntry> entries = captor.getAllValues();

        BigDecimal netMovement = entries.stream()
                .map(e -> e.getEntryType() == EntryType.CREDIT ? e.getAmount() : e.getAmount().negate())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        assertThat(netMovement).isEqualByComparingTo(acc.getBalance());
        assertThat(acc.getBalance()).isEqualByComparingTo("650.00");
    }
}
