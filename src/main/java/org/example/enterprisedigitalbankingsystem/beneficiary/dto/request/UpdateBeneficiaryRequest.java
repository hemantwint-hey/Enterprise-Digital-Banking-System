package org.example.enterprisedigitalbankingsystem.beneficiary.dto.request;

import jakarta.validation.constraints.NotBlank;
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
public class UpdateBeneficiaryRequest {

    @NotBlank(message = "Nickname is required")
    @Size(max = 100, message = "Nickname cannot exceed 100 characters")
    private String nickName;
}
