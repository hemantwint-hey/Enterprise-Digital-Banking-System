package org.example.enterprisedigitalbankingsystem.transaction.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.enterprisedigitalbankingsystem.account.entity.Account;
import org.example.enterprisedigitalbankingsystem.account.entity.AccountStatus;
import org.example.enterprisedigitalbankingsystem.account.repository.AccountRepository;
import org.example.enterprisedigitalbankingsystem.exception.BadRequestException;
import org.example.enterprisedigitalbankingsystem.exception.ResourceNotFoundException;
import org.example.enterprisedigitalbankingsystem.transaction.dto.request.DepositRequest;
import org.example.enterprisedigitalbankingsystem.transaction.dto.request.TransferRequest;
import org.example.enterprisedigitalbankingsystem.transaction.dto.request.WithdrawRequest;
import org.example.enterprisedigitalbankingsystem.transaction.dto.response.TransactionResponse;
import org.example.enterprisedigitalbankingsystem.transaction.dto.response.TransactionSummaryResponse;
import org.example.enterprisedigitalbankingsystem.transaction.entity.Transaction;
import org.example.enterprisedigitalbankingsystem.transaction.entity.TransactionStatus;
import org.example.enterprisedigitalbankingsystem.transaction.entity.TransactionType;
import org.example.enterprisedigitalbankingsystem.transaction.mapper.TransactionMapper;
import org.example.enterprisedigitalbankingsystem.transaction.repository.TransactionRepository;
import org.example.enterprisedigitalbankingsystem.transaction.service.TransactionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class TransactionServiceImpl implements TransactionService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final TransactionMapper transactionMapper;

    @Override
    public TransactionResponse deposit(DepositRequest request) {

        Account account = accountRepository.findById(request.getAccountId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Account not found with id: " + request.getAccountId()));

        validateAccountActive(account);

        BigDecimal newBalance = account.getBalance().add(request.getAmount());
        account.setBalance(newBalance);
        accountRepository.save(account);

        Transaction transaction = new Transaction();
        transaction.setDestinationAccount(account);
        transaction.setAmount(request.getAmount());
        transaction.setTransactionType(TransactionType.DEPOSIT);
        transaction.setTransactionStatus(TransactionStatus.SUCCESS);
        transaction.setBalanceAfterTransaction(newBalance);
        transaction.setRemarks(request.getRemarks());
        transaction.setTransactionReference(generateUniqueTransactionReference());

        transaction = transactionRepository.save(transaction);
        return transactionMapper.toResponse(transaction);
    }

    @Override
    public TransactionResponse withdraw(WithdrawRequest request) {
        Account account  = accountRepository.findById(request.getAccountId())
                .orElseThrow(()->
                        new ResourceNotFoundException(
                           "Account not found with id:" + request.getAccountId()
                        ));

        validateAccountActive(account);

        BigDecimal currentBalance = account.getBalance();
        BigDecimal amount = request.getAmount();

        if(currentBalance.compareTo(amount) < 0){
            throw new BadRequestException("insufficient balance");
        }

        BigDecimal newBalance = currentBalance.subtract(amount);
        account.setBalance(newBalance);
        accountRepository.save(account);
        Transaction transaction = new Transaction();

        transaction.setSourceAccount(account);
        transaction.setAmount(amount);
        transaction.setTransactionType(TransactionType.WITHDRAWAL);
        transaction.setTransactionStatus(TransactionStatus.SUCCESS);
        transaction.setBalanceAfterTransaction(newBalance);
        transaction.setRemarks(request.getRemarks());
        transaction.setTransactionReference(generateUniqueTransactionReference());

        transaction = transactionRepository.save(transaction);
        return transactionMapper.toResponse(transaction);
    }

    private void validateAccountActive(Account account) {
        if (account.getAccountStatus() != AccountStatus.ACTIVE) {
            throw new BadRequestException(
                    "Account " + account.getAccountNumber() + " is " + account.getAccountStatus()
                    + " and cannot be used for transactions"
            );
        }
    }

    private String generateUniqueTransactionReference() {
        String transactionReference;

        do {
            transactionReference = "TXN" + String.format("%013d", RANDOM.nextLong(10_000_000_000000L));
        } while (transactionRepository.existsByTransactionReference(transactionReference));

        return transactionReference;
    }

    @Override
    public TransactionResponse transfer(TransferRequest request) {
        Account sourceAccount = accountRepository.findById(request.getSourceAccountId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Source account not found with id : "
                                + request.getSourceAccountId()
                        ));
        Account destinationAccount =  accountRepository.findById(request.getDestinationAccountId())
                .orElseThrow(()->
                        new ResourceNotFoundException(
                                "Destination account not found with id :"
                                +request.getDestinationAccountId()
                        ));
        if(sourceAccount.getId().equals(destinationAccount.getId())){
            throw new BadRequestException("Source and Destination cannot be same ");
        }

        validateAccountActive(sourceAccount);
        validateAccountActive(destinationAccount);

        BigDecimal amount = request.getAmount();
        if(sourceAccount.getBalance().compareTo(amount)<0){
            throw new BadRequestException(
                    "Insufficent balance"
            );
        }

        BigDecimal sourceNewBalance = sourceAccount.getBalance().subtract(amount);
        BigDecimal destinationNewBalance = destinationAccount.getBalance().add(amount);

        sourceAccount.setBalance(sourceNewBalance);
        destinationAccount.setBalance(destinationNewBalance);
        accountRepository.save(sourceAccount);
        accountRepository.save(destinationAccount);

        Transaction transaction = new Transaction();
        transaction.setSourceAccount(sourceAccount);
        transaction.setDestinationAccount(destinationAccount);
        transaction.setAmount(amount);
        transaction.setTransactionType(TransactionType.TRANSFER);
        transaction.setTransactionStatus(TransactionStatus.SUCCESS);
        transaction.setBalanceAfterTransaction(sourceNewBalance);
        transaction.setRemarks(request.getRemarks());
        transaction.setTransactionReference(generateUniqueTransactionReference());

        transaction = transactionRepository.save(transaction);
        return transactionMapper.toResponse(transaction);
    }

    @Override
    @Transactional(readOnly = true)
    public TransactionResponse getTransactionById(Long transactionId) {
       Transaction transaction = transactionRepository.findById(transactionId)
               .orElseThrow(() ->
                       new ResourceNotFoundException(
                               "Transaction not found with id: " + transactionId
                       ));
       return transactionMapper.toResponse(transaction);
    }

    @Override
    @Transactional(readOnly = true)
    public TransactionResponse getTransactionByReference(String transactionReference) {
        Transaction transaction = transactionRepository.findByTransactionReference(transactionReference)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                           "Transaction not found with reference"
                           + transactionReference
                        ));
        return transactionMapper.toResponse(transaction);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionSummaryResponse> getTransactionHistory(Long accountId) {

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Account not found with id:" + accountId
                        ));

       List<Transaction> transactions = transactionRepository.findBySourceAccountIdOrDestinationAccountIdOrderByCreatedAtDesc(
               accountId, accountId
       );
       return transactions.stream().map(transactionMapper::toSummaryResponse)
               .toList();
    }
}
