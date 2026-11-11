package org.example.enterprisedigitalbankingsystem.search;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.example.enterprisedigitalbankingsystem.account.dto.request.AccountSearchRequest;
import org.example.enterprisedigitalbankingsystem.account.entity.Account;
import org.example.enterprisedigitalbankingsystem.account.entity.AccountStatus;
import org.example.enterprisedigitalbankingsystem.account.entity.AccountType;
import org.example.enterprisedigitalbankingsystem.account.repository.AccountRepository;
import org.example.enterprisedigitalbankingsystem.account.specification.AccountSpecification;
import org.example.enterprisedigitalbankingsystem.audit.dto.request.AuditSearchRequest;
import org.example.enterprisedigitalbankingsystem.audit.entity.AuditAction;
import org.example.enterprisedigitalbankingsystem.audit.entity.AuditLog;
import org.example.enterprisedigitalbankingsystem.audit.repository.AuditRepository;
import org.example.enterprisedigitalbankingsystem.audit.specification.AuditSpecification;
import org.example.enterprisedigitalbankingsystem.auth.entity.Role;
import org.example.enterprisedigitalbankingsystem.auth.entity.User;
import org.example.enterprisedigitalbankingsystem.auth.entity.UserStatus;
import org.example.enterprisedigitalbankingsystem.auth.repository.UserRepository;
import org.example.enterprisedigitalbankingsystem.customer.dto.request.CustomerSearchRequest;
import org.example.enterprisedigitalbankingsystem.customer.entity.Customer;
import org.example.enterprisedigitalbankingsystem.customer.entity.KYCStatus;
import org.example.enterprisedigitalbankingsystem.customer.repository.CustomerRepository;
import org.example.enterprisedigitalbankingsystem.customer.specification.CustomerSpecification;
import org.example.enterprisedigitalbankingsystem.ledger.entity.EntryType;
import org.example.enterprisedigitalbankingsystem.ledger.entity.LedgerEntry;
import org.example.enterprisedigitalbankingsystem.ledger.repository.LedgerRepository;
import org.example.enterprisedigitalbankingsystem.transaction.dto.request.TransactionSearchRequest;
import org.example.enterprisedigitalbankingsystem.transaction.entity.Transaction;
import org.example.enterprisedigitalbankingsystem.transaction.entity.TransactionStatus;
import org.example.enterprisedigitalbankingsystem.transaction.entity.TransactionType;
import org.example.enterprisedigitalbankingsystem.transaction.repository.TransactionRepository;
import org.example.enterprisedigitalbankingsystem.transaction.specification.TransactionSpecification;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest(properties = {
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.generate_statistics=true"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class DynamicSearchAndJpaVerificationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private LedgerRepository ledgerRepository;

    @Autowired
    private AuditRepository auditRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    private Customer seedCustomer(String suffix, String firstName, String city, KYCStatus kyc) {
        User user = userRepository.save(User.builder()
                .fullName(firstName + " Test")
                .username("user_" + suffix)
                .email("user_" + suffix + "@bank.com")
                .password("encoded-pass")
                .phoneNumber("999000" + suffix)
                .role(Role.CUSTOMER)
                .status(UserStatus.ACTIVE)
                .kycVerified(kyc == KYCStatus.VERIFIED)
                .emailVerified(true)
                .phoneVerified(true)
                .build());

        return customerRepository.save(Customer.builder()
                .user(user)
                .firstName(firstName)
                .lastName("Tester")
                .dateOfBirth(LocalDate.of(1995, 5, 10))
                .phoneNumber("999111" + suffix)
                .address("123 Main Street")
                .city(city)
                .state("Maharashtra")
                .country("India")
                .postalCode("400001")
                .kycStatus(kyc)
                .panNumber("ABCDE" + suffix + "F")
                .aadhaarNumber("12345678" + suffix)
                .build());
    }

    @Test
    @DisplayName("Day 79: Dynamic specifications filter accurately and escape LIKE wildcards safely")
    void specifications_filterAndEscapeWildcards() {
        Customer alice = seedCustomer("1001", "Alice%Check", "Mumbai", KYCStatus.VERIFIED);
        Customer bob = seedCustomer("1002", "AlicePlain", "Pune", KYCStatus.PENDING);

        Account acc1 = accountRepository.save(Account.builder()
                .accountNumber("ACC-1001")
                .customer(alice)
                .accountHolderName("Alice%Check Tester")
                .branch("MUMBAI_FORT")
                .balance(new BigDecimal("5000.00"))
                .accountType(AccountType.SAVINGS)
                .accountStatus(AccountStatus.ACTIVE)
                .build());

        Account acc2 = accountRepository.save(Account.builder()
                .accountNumber("ACC-1002")
                .customer(bob)
                .accountHolderName("AlicePlain Tester")
                .branch("MUMBAIFORT")
                .balance(new BigDecimal("1500.00"))
                .accountType(AccountType.CURRENT)
                .accountStatus(AccountStatus.FROZEN)
                .build());

        Transaction deposit = transactionRepository.save(Transaction.builder()
                .transactionReference("TXN-DEP-1")
                .sourceAccount(null)
                .destinationAccount(acc1)
                .amount(new BigDecimal("5000.00"))
                .transactionType(TransactionType.DEPOSIT)
                .transactionStatus(TransactionStatus.SUCCESS)
                .balanceAfterTransaction(new BigDecimal("5000.00"))
                .remarks("Initial 100% salary credit")
                .build());

        Transaction transfer = transactionRepository.save(Transaction.builder()
                .transactionReference("TXN-TRF-2")
                .sourceAccount(acc1)
                .destinationAccount(acc2)
                .amount(new BigDecimal("250.00"))
                .transactionType(TransactionType.TRANSFER)
                .transactionStatus(TransactionStatus.SUCCESS)
                .balanceAfterTransaction(new BigDecimal("4750.00"))
                .remarks("Regular bill payment")
                .build());

        AuditLog audit1 = auditRepository.save(AuditLog.builder()
                .performedBy(alice.getUser())
                .action(AuditAction.CREATE)
                .entityName("Account")
                .entityId(acc1.getId().toString())
                .description("Opened 50%_bonus account")
                .ipAddress("127.0.0.1")
                .build());

        entityManager.flush();
        entityManager.clear();

        // Wildcard '%' in Customer firstName filter must match only "Alice%Check", not "AlicePlain"
        Page<Customer> custMatches = customerRepository.findAll(
                CustomerSpecification.withFilters(CustomerSearchRequest.builder()
                        .firstName("%check")
                        .kycStatus(KYCStatus.VERIFIED)
                        .build()),
                PageRequest.of(0, 10)
        );
        assertEquals(1, custMatches.getTotalElements());
        assertEquals(alice.getId(), custMatches.getContent().get(0).getId());

        // Wildcard '_' in Account branch filter must match literal underscore "MUMBAI_FORT", not "MUMBAIFORT"
        Page<Account> accMatches = accountRepository.findAll(
                AccountSpecification.withFilters(AccountSearchRequest.builder()
                        .branch("MUMBAI_")
                        .minBalance(new BigDecimal("2000.00"))
                        .build()),
                PageRequest.of(0, 10)
        );
        assertEquals(1, accMatches.getTotalElements());
        assertEquals(acc1.getId(), accMatches.getContent().get(0).getId());

        // Transaction search by accountId includes both DEPOSIT (null sourceAccount) and TRANSFER
        Page<Transaction> acc1Txns = transactionRepository.findAll(
                TransactionSpecification.withFilters(TransactionSearchRequest.builder()
                        .accountId(acc1.getId())
                        .build()),
                PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "id"))
        );
        assertEquals(2, acc1Txns.getTotalElements());

        // Wildcard '%' in Transaction remarks filter
        Page<Transaction> pctTxns = transactionRepository.findAll(
                TransactionSpecification.withFilters(TransactionSearchRequest.builder()
                        .remarksKeyword("100%")
                        .build()),
                PageRequest.of(0, 10)
        );
        assertEquals(1, pctTxns.getTotalElements());
        assertEquals(deposit.getId(), pctTxns.getContent().get(0).getId());

        // Wildcard '%_' in AuditLog description filter
        Page<AuditLog> auditMatches = auditRepository.findAll(
                AuditSpecification.withFilters(AuditSearchRequest.builder()
                        .descriptionKeyword("50%_bonus")
                        .action(AuditAction.CREATE)
                        .build()),
                PageRequest.of(0, 10)
        );
        assertEquals(1, auditMatches.getTotalElements());
        assertEquals(audit1.getId(), auditMatches.getContent().get(0).getId());
    }

    @Test
    @DisplayName("Day 82: Stale Account version throws ObjectOptimisticLockingFailureException on concurrent update")
    void accountVersion_whenStaleCopySaved_throwsOptimisticLockingFailureException() {
        Customer customer = seedCustomer("2001", "Optimistic", "Delhi", KYCStatus.VERIFIED);
        Account account = accountRepository.saveAndFlush(Account.builder()
                .accountNumber("ACC-OPT-1")
                .customer(customer)
                .accountHolderName("Optimistic Tester")
                .branch("DELHI_MAIN")
                .balance(new BigDecimal("1000.00"))
                .accountType(AccountType.SAVINGS)
                .accountStatus(AccountStatus.ACTIVE)
                .build());

        Long accountId = account.getId();
        entityManager.clear();

        Account staleCopy = accountRepository.findById(accountId).orElseThrow();
        entityManager.detach(staleCopy);

        Account freshCopy = accountRepository.findById(accountId).orElseThrow();
        freshCopy.setBalance(new BigDecimal("1200.00"));
        accountRepository.saveAndFlush(freshCopy);
        entityManager.clear();

        staleCopy.setBalance(new BigDecimal("900.00"));
        assertThrows(
                ObjectOptimisticLockingFailureException.class,
                () -> accountRepository.saveAndFlush(staleCopy)
        );
    }

    @Test
    @DisplayName("Day 83: EntityGraph prevents N+1 queries on LedgerRepository and AuditRepository")
    void entityGraph_eliminatesNPlusOneQueries() {
        Customer customer = seedCustomer("3001", "Graph", "Bengaluru", KYCStatus.VERIFIED);
        Account account = accountRepository.save(Account.builder()
                .accountNumber("ACC-GRP-1")
                .customer(customer)
                .accountHolderName("Graph Tester")
                .branch("BLR_MAIN")
                .balance(new BigDecimal("3000.00"))
                .accountType(AccountType.SAVINGS)
                .accountStatus(AccountStatus.ACTIVE)
                .build());

        for (int i = 1; i <= 5; i++) {
            Transaction tx = transactionRepository.save(Transaction.builder()
                    .transactionReference("TXN-GRP-" + i)
                    .destinationAccount(account)
                    .amount(new BigDecimal("100.00"))
                    .transactionType(TransactionType.DEPOSIT)
                    .transactionStatus(TransactionStatus.SUCCESS)
                    .balanceAfterTransaction(new BigDecimal("3000.00"))
                    .remarks("Entry " + i)
                    .build());

            ledgerRepository.save(LedgerEntry.builder()
                    .transaction(tx)
                    .account(account)
                    .entryType(EntryType.CREDIT)
                    .amount(new BigDecimal("100.00"))
                    .balanceAfterEntry(new BigDecimal("3000.00"))
                    .narration("Ledger " + i)
                    .build());

            auditRepository.save(AuditLog.builder()
                    .performedBy(customer.getUser())
                    .action(AuditAction.CREATE)
                    .entityName("Transaction")
                    .entityId("TX-" + i)
                    .description("Audit " + i)
                    .ipAddress("127.0.0.1")
                    .build());
        }

        entityManager.flush();
        entityManager.clear();

        Statistics stats = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
        stats.setStatisticsEnabled(true);
        stats.clear();

        Page<LedgerEntry> ledgerPage = ledgerRepository.findByAccountId(account.getId(), PageRequest.of(0, 10));
        assertEquals(5, ledgerPage.getContent().size());
        for (LedgerEntry entry : ledgerPage.getContent()) {
            assertEquals("ACC-GRP-1", entry.getAccount().getAccountNumber());
            assertTrue(entry.getTransaction().getTransactionReference().startsWith("TXN-GRP-"));
        }
        long ledgerQueries = stats.getPrepareStatementCount();
        assertTrue(ledgerQueries <= 2, "Expected <= 2 SQL statements (1 EntityGraph select + optional count), got " + ledgerQueries);

        entityManager.clear();
        stats.clear();

        Page<AuditLog> auditPage = auditRepository.findAll(
                AuditSpecification.withFilters(AuditSearchRequest.builder().entityName("Transaction").build()),
                PageRequest.of(0, 10)
        );
        assertEquals(5, auditPage.getContent().size());
        for (AuditLog log : auditPage.getContent()) {
            assertEquals("user_3001", log.getPerformedBy().getUsername());
        }
        long auditQueries = stats.getPrepareStatementCount();
        assertTrue(auditQueries <= 2, "Expected <= 2 SQL statements for AuditLog EntityGraph query, got " + auditQueries);
    }
}

