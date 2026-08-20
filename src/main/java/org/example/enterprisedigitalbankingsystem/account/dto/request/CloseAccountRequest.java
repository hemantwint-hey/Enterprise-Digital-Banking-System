package org.example.enterprisedigitalbankingsystem.account.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CloseAccountRequest {
    @NotBlank(message = "Close reason is required")
    @Size(max = 255, message = "Close reason cannot exceed 255 characters")
    private String reason;
}
