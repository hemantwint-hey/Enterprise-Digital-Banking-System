package org.example.enterprisedigitalbankingsystem.emi.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.enterprisedigitalbankingsystem.loan.entity.Loan;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "emis")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Emi {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_id", nullable = false)
    private Loan loan;

    @Column(nullable = false)
    private Integer installmentNumber;

    @Column(nullable = false)
    private LocalDate dueDate;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EmiStatus status;

    private LocalDateTime paidAt;

    @PrePersist
    public void prePersist() {
        if (status == null) status = EmiStatus.PENDING;
    }
}
