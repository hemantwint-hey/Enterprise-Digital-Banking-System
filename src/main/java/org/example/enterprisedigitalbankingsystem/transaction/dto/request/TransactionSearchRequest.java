package org.example.enterprisedigitalbankingsystem.transaction.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.enterprisedigitalbankingsystem.transaction.entity.TransactionStatus;
import org.example.enterprisedigitalbankingsystem.transaction.entity.TransactionType;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionSearchRequest {

    @Positive(message = "Account id must be positive")
    private Long accountId;

    @Size(max = 64, message = "Transaction reference cannot exceed 64 characters")
    private String transactionReference;

    private TransactionType transactionType;
    private TransactionStatus transactionStatus;

    @DecimalMin(value = "0.01", message = "Minimum amount must be at least 0.01")
    @Digits(integer = 16, fraction = 2, message = "Minimum amount supports up to 2 decimal places")
    private BigDecimal minAmount;

    @DecimalMin(value = "0.01", message = "Maximum amount must be at least 0.01")
    @Digits(integer = 16, fraction = 2, message = "Maximum amount supports up to 2 decimal places")
    private BigDecimal maxAmount;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime fromDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime toDate;

    @Size(max = 100, message = "Remarks keyword cannot exceed 100 characters")
    private String remarksKeyword;

    @JsonIgnore
    @AssertTrue(message = "minAmount cannot exceed maxAmount")
    public boolean isAmountRangeValid() {
        return minAmount == null || maxAmount == null || minAmount.compareTo(maxAmount) <= 0;
    }

    @JsonIgnore
    @AssertTrue(message = "fromDate cannot be after toDate")
    public boolean isDateRangeValid() {
        return fromDate == null || toDate == null || !fromDate.isAfter(toDate);
    }
}

