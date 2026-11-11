package org.example.enterprisedigitalbankingsystem.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

/**
 * Day 80: OpenAPI 3 / Swagger UI configuration with Bearer JWT security scheme.
 */
@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Enterprise Digital Banking System API",
                version = "1.0.0",
                description = "Core banking REST API covering JWT authentication, customers & KYC, accounts, double-entry ledger, fund transfers, beneficiaries, loans & EMIs, and audit logs."
        ),
        security = @SecurityRequirement(name = "bearerAuth")
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "Provide a valid JWT access token obtained from POST /auth/login"
)
public class OpenApiConfig {
}

