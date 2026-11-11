package org.example.enterprisedigitalbankingsystem.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class RefreshTokenRequest {

    @NotBlank(message = "Refresh token is required")
    @Size(max = 512, message = "Refresh token cannot exceed 512 characters")
    private String refreshToken;
}
