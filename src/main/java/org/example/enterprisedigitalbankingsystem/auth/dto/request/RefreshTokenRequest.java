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
    @NotBlank
    private String refreshToken;
}
