package org.example.enterprisedigitalbankingsystem.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginRequest {
    @Email(message = "Email format is invalid")
    @NotBlank(message = "Email is Required")
    private String email;

    @NotBlank(message = "Password is required")
    private String password;
}
