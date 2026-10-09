package org.example.enterprisedigitalbankingsystem.loan.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.enterprisedigitalbankingsystem.account.entity.Account;
import org.example.enterprisedigitalbankingsystem.customer.entity.Customer;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Table(name ="loans")
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Loan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "customer_id" , nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Customer customer;

    @ManyToOne( fetch = FetchType.LAZY)
    @JoinColumn(name = "disbursement_account_id" , nullable = false)
    private Account   disbursementAccount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LoanType loanType;

    @Column(nullable = false , precision = 18 , scale = 2)
    private BigDecimal principalAmount;

    @Column(nullable = false , precision = 5 , scale = 2)
    private BigDecimal interestRate;

    @Column (nullable = false )
    private Integer tenureMonths;

    @Column(nullable = false , precision =  18 , scale = 2)
    private BigDecimal emiAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LoanStatus status;

    @Column(nullable = false , updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime approvedAt;
    private LocalDateTime closedAt;

    @PrePersist
    public void prePersist(){
        createdAt = LocalDateTime.now();
        if(status == null) status = LoanStatus.PENDING;
    }

}
