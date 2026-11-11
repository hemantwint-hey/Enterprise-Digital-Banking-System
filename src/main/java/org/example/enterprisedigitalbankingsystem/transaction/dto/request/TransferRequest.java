package org.example.enterprisedigitalbankingsystem.transaction.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransferRequest {

    @NotNull(message = "Source account id is required")
    @Positive(message = "Source account id must be positive")
    private Long sourceAccountId;

    @Positive(message = "Destination account id must be positive")
    private Long destinationAccountId;

    @Positive(message = "Beneficiary id must be positive")
    private Long beneficiaryId;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "1.00", message = "Amount must be at least 1.00")
    @DecimalMax(value = "10000000.00", message = "Single transfer limit is 10,000,000.00")
    @Digits(integer = 16, fraction = 2, message = "Amount must have at most 2 decimal places")
    private BigDecimal amount;

    @Size(max = 255, message = "Remarks cannot exceed 255 characters")
    private String remarks;

    @JsonIgnore
    @AssertTrue(message = "Provide exactly one of destinationAccountId or beneficiaryId")
    public boolean isTargetExclusive() {
        return (destinationAccountId != null) ^ (beneficiaryId != null);
    }

    @JsonIgnore
    @AssertTrue(message = "Source and destination account cannot be the same")
    public boolean isNotSelfTransfer() {
        if (sourceAccountId == null || destinationAccountId == null) {
            return true;
        }
        return !sourceAccountId.equals(destinationAccountId);
    }
}
