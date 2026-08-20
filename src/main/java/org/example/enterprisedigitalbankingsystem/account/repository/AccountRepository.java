package org.example.enterprisedigitalbankingsystem.account.repository;

import org.example.enterprisedigitalbankingsystem.account.entity.Account;
import org.example.enterprisedigitalbankingsystem.account.entity.AccountStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.example.enterprisedigitalbankingsystem.account.entity.AccountType;


import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {

    Optional<Account> findByAccountNumber(String accountNumber);

    List<Account> findByCustomerId(Long customerId);

    Optional<Account> findByIdAndCustomerId(Long accountId, Long customerId);

    boolean existsByAccountNumber(String accountNumber);
    List<Account> findByAccountStatus(AccountStatus accountStatus);

    List<Account> findByCustomerIdAndAccountStatus(Long customerId, AccountStatus accountStatus);

    List<Account> findByCustomerIdAndAccountType(Long customerId, AccountType accountType);
}