package org.example.enterprisedigitalbankingsystem.account.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.example.enterprisedigitalbankingsystem.account.entity.AccountType;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateAccountRequest {
        @NotBlank(message = "Branch is required")
        @Size(max = 100, message = "Branch cannot exceed 100 characters")
        private String branch;

        @NotNull(message = "Account type should not be null")
        private AccountType accountType;
}
