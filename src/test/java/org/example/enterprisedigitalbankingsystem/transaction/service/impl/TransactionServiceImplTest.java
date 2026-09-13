package org.example.enterprisedigitalbankingsystem.transaction.service.impl;

import org.example.enterprisedigitalbankingsystem.account.entity.Account;
import org.example.enterprisedigitalbankingsystem.account.entity.AccountStatus;
import org.example.enterprisedigitalbankingsystem.account.entity.AccountType;
import org.example.enterprisedigitalbankingsystem.account.repository.AccountRepository;
import org.example.enterprisedigitalbankingsystem.audit.service.AuditService;
import org.example.enterprisedigitalbankingsystem.exception.BadRequestException;
import org.example.enterprisedigitalbankingsystem.exception.ResourceNotFoundException;
import org.example.enterprisedigitalbankingsystem.ledger.service.LedgerService;
import org.example.enterprisedigitalbankingsystem.transaction.dto.request.DepositRequest;
import org.example.enterprisedigitalbankingsystem.transaction.dto.request.TransferRequest;
import org.example.enterprisedigitalbankingsystem.transaction.dto.request.WithdrawRequest;
import org.example.enterprisedigitalbankingsystem.transaction.dto.response.TransactionResponse;
import org.example.enterprisedigitalbankingsystem.transaction.entity.Transaction;
import org.example.enterprisedigitalbankingsystem.transaction.mapper.TransactionMapper;
import org.example.enterprisedigitalbankingsystem.transaction.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Day 36 - Transaction/Ledger testing: verifies balance mutation, validation
 * edge cases, and that every successful transaction hands off to LedgerService.
 */
@ExtendWith(MockitoExtension.class)
class TransactionServiceImplTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private LedgerService ledgerService;

    @Mock
    private AuditService auditService;

    private TransactionServiceImpl transactionService;

    private Account sourceAccount;
    private Account destinationAccount;

    @BeforeEach
    void setUp() {
        transactionService = new TransactionServiceImpl(
                transactionRepository, accountRepository, new TransactionMapper(), ledgerService, auditService);

        sourceAccount = Account.builder()
                .id(1L)
                .accountNumber("ACC-SOURCE-001")
                .accountHolderName("Alice")
                .branch("Main")
                .balance(new BigDecimal("1000.00"))
                .accountType(AccountType.SAVINGS)
                .accountStatus(AccountStatus.ACTIVE)
                .build();

        destinationAccount = Account.builder()
                .id(2L)
                .accountNumber("ACC-DEST-002")
                .accountHolderName("Bob")
                .branch("Main")
                .balance(new BigDecimal("200.00"))
                .accountType(AccountType.SAVINGS)
                .accountStatus(AccountStatus.ACTIVE)
                .build();
    }

    private void stubTransactionPersistence() {
        when(transactionRepository.existsByTransactionReference(anyString())).thenReturn(false);
        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(invocation -> {
                    Transaction transaction = invocation.getArgument(0);
                    if (transaction.getId() == null) {
                        transaction.setId(1L);
                    }
                    return transaction;
                });
    }

    // ---------------- Deposit ----------------

    @Test
    void deposit_success_creditsAccountAndRecordsLedgerEntry() {
        when(accountRepository.findById(2L)).thenReturn(Optional.of(destinationAccount));
        stubTransactionPersistence();

        DepositRequest request = DepositRequest.builder()
                .accountId(2L)
                .amount(new BigDecimal("300.00"))
                .remarks("Salary")
                .build();

        TransactionResponse response = transactionService.deposit(request);

        assertThat(destinationAccount.getBalance()).isEqualByComparingTo("500.00");
        assertThat(response.getBalanceAfterTransaction()).isEqualByComparingTo("500.00");
        verify(accountRepository).save(destinationAccount);
        verify(ledgerService).recordTransactionEntries(any(Transaction.class));
    }

    @Test
    void deposit_accountNotFound_throwsResourceNotFoundException() {
        when(accountRepository.findById(99L)).thenReturn(Optional.empty());

        DepositRequest request = DepositRequest.builder().accountId(99L).amount(BigDecimal.TEN).build();

        assertThatThrownBy(() -> transactionService.deposit(request))
                .isInstanceOf(ResourceNotFoundException.class);
        verifyNoInteractions(ledgerService);
    }

    @Test
    void deposit_frozenAccount_throwsBadRequestExceptionAndSkipsLedger() {
        destinationAccount.setAccountStatus(AccountStatus.FROZEN);
        when(accountRepository.findById(2L)).thenReturn(Optional.of(destinationAccount));

        DepositRequest request = DepositRequest.builder().accountId(2L).amount(BigDecimal.TEN).build();

        assertThatThrownBy(() -> transactionService.deposit(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("FROZEN");
        verifyNoInteractions(ledgerService);
    }

    // ---------------- Withdraw ----------------

    @Test
    void withdraw_success_debitsAccountAndRecordsLedgerEntry() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(sourceAccount));
        stubTransactionPersistence();

        WithdrawRequest request = WithdrawRequest.builder()
                .accountId(1L)
                .amount(new BigDecimal("400.00"))
                .build();

        TransactionResponse response = transactionService.withdraw(request);

        assertThat(sourceAccount.getBalance()).isEqualByComparingTo("600.00");
        assertThat(response.getBalanceAfterTransaction()).isEqualByComparingTo("600.00");
        verify(ledgerService).recordTransactionEntries(any(Transaction.class));
    }

    @Test
    void withdraw_insufficientBalance_throwsBadRequestExceptionAndSkipsLedger() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(sourceAccount));

        WithdrawRequest request = WithdrawRequest.builder()
                .accountId(1L)
                .amount(new BigDecimal("5000.00"))
                .build();

        assertThatThrownBy(() -> transactionService.withdraw(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("insufficient balance");

        assertThat(sourceAccount.getBalance()).isEqualByComparingTo("1000.00");
        verifyNoInteractions(ledgerService);
    }

    // ---------------- Transfer ----------------

    @Test
    void transfer_success_movesFundsAndRecordsDoubleEntry() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(sourceAccount));
        when(accountRepository.findById(2L)).thenReturn(Optional.of(destinationAccount));
        stubTransactionPersistence();

        TransferRequest request = TransferRequest.builder()
                .sourceAccountId(1L)
                .destinationAccountId(2L)
                .amount(new BigDecimal("250.00"))
                .build();

        TransactionResponse response = transactionService.transfer(request);

        assertThat(sourceAccount.getBalance()).isEqualByComparingTo("750.00");
        assertThat(destinationAccount.getBalance()).isEqualByComparingTo("450.00");
        assertThat(response.getAmount()).isEqualByComparingTo("250.00");
        verify(ledgerService).recordTransactionEntries(any(Transaction.class));
    }

    @Test
    void transfer_sameSourceAndDestination_throwsBadRequestException() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(sourceAccount));

        TransferRequest request = TransferRequest.builder()
                .sourceAccountId(1L)
                .destinationAccountId(1L)
                .amount(BigDecimal.TEN)
                .build();

        assertThatThrownBy(() -> transactionService.transfer(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Source and Destination cannot be same");
        verifyNoInteractions(ledgerService);
    }

    @Test
    void transfer_insufficientBalance_throwsBadRequestExceptionAndLeavesBalancesUntouched() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(sourceAccount));
        when(accountRepository.findById(2L)).thenReturn(Optional.of(destinationAccount));

        TransferRequest request = TransferRequest.builder()
                .sourceAccountId(1L)
                .destinationAccountId(2L)
                .amount(new BigDecimal("9999.00"))
                .build();

        assertThatThrownBy(() -> transactionService.transfer(request))
                .isInstanceOf(BadRequestException.class);

        assertThat(sourceAccount.getBalance()).isEqualByComparingTo("1000.00");
        assertThat(destinationAccount.getBalance()).isEqualByComparingTo("200.00");
        verifyNoInteractions(ledgerService);
    }

    @Test
    void transfer_closedDestinationAccount_throwsBadRequestException() {
        destinationAccount.setAccountStatus(AccountStatus.CLOSED);
        when(accountRepository.findById(1L)).thenReturn(Optional.of(sourceAccount));
        when(accountRepository.findById(2L)).thenReturn(Optional.of(destinationAccount));

        TransferRequest request = TransferRequest.builder()
                .sourceAccountId(1L)
                .destinationAccountId(2L)
                .amount(BigDecimal.TEN)
                .build();

        assertThatThrownBy(() -> transactionService.transfer(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("CLOSED");
        verifyNoInteractions(ledgerService);
    }
}
