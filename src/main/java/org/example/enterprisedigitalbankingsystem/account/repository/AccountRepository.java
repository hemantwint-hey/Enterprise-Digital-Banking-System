package org.example.enterprisedigitalbankingsystem.account.repository;

import org.example.enterprisedigitalbankingsystem.account.entity.Account;
import org.example.enterprisedigitalbankingsystem.account.entity.AccountStatus;
import org.example.enterprisedigitalbankingsystem.account.entity.AccountType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long>, JpaSpecificationExecutor<Account> {

    @EntityGraph(attributePaths = {"customer"})
    @Override
    Optional<Account> findById(Long id);

    @EntityGraph(attributePaths = {"customer"})
    Optional<Account> findByAccountNumber(String accountNumber);

    Page<Account> findByCustomerId(Long customerId, Pageable pageable);

    Optional<Account> findByIdAndCustomerId(Long accountId, Long customerId);

    boolean existsByAccountNumber(String accountNumber);

    List<Account> findByAccountStatus(AccountStatus accountStatus);

    List<Account> findByCustomerIdAndAccountStatus(Long customerId, AccountStatus accountStatus);

    List<Account> findByCustomerIdAndAccountType(Long customerId, AccountType accountType);
}
