package org.example.enterprisedigitalbankingsystem.account.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.enterprisedigitalbankingsystem.account.dto.request.CloseAccountRequest;
import org.example.enterprisedigitalbankingsystem.account.dto.request.CreateAccountRequest;
import org.example.enterprisedigitalbankingsystem.account.dto.request.FreezeAccountRequest;
import org.example.enterprisedigitalbankingsystem.account.dto.request.UpdateAccountRequest;
import org.example.enterprisedigitalbankingsystem.account.dto.response.AccountResponse;
import org.example.enterprisedigitalbankingsystem.account.dto.response.AccountSummaryResponse;
import org.example.enterprisedigitalbankingsystem.account.entity.Account;
import org.example.enterprisedigitalbankingsystem.account.entity.AccountStatus;
import org.example.enterprisedigitalbankingsystem.account.mapper.AccountMapper;
import org.example.enterprisedigitalbankingsystem.account.repository.AccountRepository;
import org.example.enterprisedigitalbankingsystem.account.service.AccountService;
import org.example.enterprisedigitalbankingsystem.audit.entity.AuditAction;
import org.example.enterprisedigitalbankingsystem.audit.service.AuditService;
import org.example.enterprisedigitalbankingsystem.customer.entity.Customer;
import org.example.enterprisedigitalbankingsystem.customer.repository.CustomerRepository;
import org.example.enterprisedigitalbankingsystem.exception.BadRequestException;
import org.example.enterprisedigitalbankingsystem.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AccountServiceImpl implements AccountService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;
    private final AccountMapper accountMapper;
    private final AuditService auditService;

    @Override
    public AccountResponse createAccount(CreateAccountRequest request) {
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + request.getCustomerId()));

        Account account = Account.builder()
                .accountNumber(generateUniqueAccountNumber())
                .customer(customer)
                .accountHolderName(accountMapper.buildCustomerName(customer))
                .branch(request.getBranch())
                .balance(request.getOpeningBalance())
                .accountType(request.getAccountType())
                .accountStatus(AccountStatus.ACTIVE)
                .build();

        account = accountRepository.save(account);

        auditService.log(AuditAction.CREATE, "Account", account.getId().toString(),
                null, "accountNumber=" + account.getAccountNumber() + ", balance=" + account.getBalance(),
                "Account created");

        return accountMapper.toResponse(account);
    }

    @Override
    @Transactional(readOnly = true)
    public AccountResponse getAccountById(Long accountId) {
        return accountMapper.toResponse(findAccountById(accountId));
    }

    @Override
    @Transactional(readOnly = true)
    public AccountResponse getAccountByAccountNumber(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with account number: " + accountNumber));

        return accountMapper.toResponse(account);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccountSummaryResponse> getAccountsByCustomerId(Long customerId) {
        if (!customerRepository.existsById(customerId)) {
            throw new ResourceNotFoundException("Customer not found with id: " + customerId);
        }

        return accountRepository.findByCustomerId(customerId)
                .stream()
                .map(accountMapper::toSummaryResponse)
                .toList();
    }

    @Override
    public AccountResponse updateAccount(Long accountId, UpdateAccountRequest request) {
        Account account = findAccountById(accountId);

        validateAccountNotClosed(account);
        accountMapper.updateEntity(account, request);
        account = accountRepository.save(account);

        auditService.log(AuditAction.UPDATE, "Account", account.getId().toString(),
                null, null, "Account details updated");

        return accountMapper.toResponse(account);
    }

    @Override
    public AccountResponse freezeAccount(Long accountId, FreezeAccountRequest request) {
        Account account = findAccountById(accountId);

        validateAccountNotClosed(account);
        if (account.getAccountStatus() == AccountStatus.FROZEN) {
            throw new BadRequestException("Account is already frozen");
        }

        AccountStatus oldStatus = account.getAccountStatus();
        account.setAccountStatus(AccountStatus.FROZEN);
        account = accountRepository.save(account);

        auditService.log(AuditAction.UPDATE, "Account", account.getId().toString(),
                oldStatus.name(), AccountStatus.FROZEN.name(), "Account frozen");

        return accountMapper.toResponse(account);
    }

    @Override
    public AccountResponse closeAccount(Long accountId, CloseAccountRequest request) {
        Account account = findAccountById(accountId);

        validateAccountNotClosed(account);
        if (account.getBalance().compareTo(BigDecimal.ZERO) != 0) {
            throw new BadRequestException("Account balance must be zero before closing");
        }

        AccountStatus oldStatus = account.getAccountStatus();
        account.setAccountStatus(AccountStatus.CLOSED);
        account = accountRepository.save(account);

        auditService.log(AuditAction.UPDATE, "Account", account.getId().toString(),
                oldStatus.name(), AccountStatus.CLOSED.name(), "Account closed");

        return accountMapper.toResponse(account);
    }

    private Account findAccountById(Long accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with id: " + accountId));
    }

    private void validateAccountNotClosed(Account account) {
        if (account.getAccountStatus() == AccountStatus.CLOSED) {
            throw new BadRequestException("Closed account cannot be modified");
        }
    }

    private String generateUniqueAccountNumber() {
        String accountNumber;

        do {
            accountNumber = "10" + String.format("%010d", RANDOM.nextLong(10_000_000_000L));
        } while (accountRepository.existsByAccountNumber(accountNumber));

        return accountNumber;
    }
}
