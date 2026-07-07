package org.example.enterprisedigitalbankingsystem.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ForgotPasswordRequest {
    @Email(message ="Invalid Email Format ")
    @NotBlank
    private String email;
}
