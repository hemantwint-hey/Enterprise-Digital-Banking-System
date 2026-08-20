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
public class FreezeAccountRequest {
    @NotBlank(message = "Freeze reason is required")
    @Size(max = 255, message = "Freeze reason cannot exceed 255 characters")
    private String reason;
}
