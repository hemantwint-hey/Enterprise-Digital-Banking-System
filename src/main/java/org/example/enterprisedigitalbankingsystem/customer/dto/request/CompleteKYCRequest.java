package org.example.enterprisedigitalbankingsystem.customer.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompleteKYCRequest {

    @NotBlank(message = "PAN number is required")
    @Pattern(
            regexp = "^[A-Z]{5}[0-9]{4}[A-Z]{1}$",
            message = "Invalid PAN number format"
    )
    private String panNumber;

    @NotBlank(message = "Aadhaar number is required")
    @Pattern(
            regexp = "^\\d{12}$",
            message = "Aadhaar number must contain exactly 12 digits"
    )
    private String aadhaarNumber;
}