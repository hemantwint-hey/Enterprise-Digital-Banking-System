package org.example.enterprisedigitalbankingsystem.emi.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.enterprisedigitalbankingsystem.emi.entity.EmiStatus;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmiResponse implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private Long id;
    private Long loanId;
    private Integer installmentNumber;
    private LocalDate dueDate;
    private BigDecimal amount;
    private EmiStatus status;
    private LocalDateTime paidAt;
}
