package org.example.enterprisedigitalbankingsystem.beneficiary.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateBeneficiaryRequest {

    @NotNull(message = "Customer id is required")
    @Positive(message = "Customer id must be positive")
    private Long customerId;

    @NotNull(message = "Beneficiary account id is required")
    @Positive(message = "Beneficiary account id must be positive")
    private Long beneficiaryAccountId;

    @NotBlank(message = "Nickname is required")
    @Size(min = 2, max = 100, message = "Nickname must be between 2 and 100 characters")
    private String nickName;
}
