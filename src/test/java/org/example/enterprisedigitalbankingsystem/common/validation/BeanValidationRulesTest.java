package org.example.enterprisedigitalbankingsystem.common.validation;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.example.enterprisedigitalbankingsystem.auth.dto.request.ChangePassowordRequest;
import org.example.enterprisedigitalbankingsystem.auth.dto.request.RegisterRequest;
import org.example.enterprisedigitalbankingsystem.auth.dto.request.ResetPasswordRequest;
import org.example.enterprisedigitalbankingsystem.customer.dto.request.CreateCustomerRequest;
import org.example.enterprisedigitalbankingsystem.transaction.dto.request.TransferRequest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class BeanValidationRulesTest {

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void closeValidatorFactory() {
        if (validatorFactory != null) {
            validatorFactory.close();
        }
    }

    @Test
    @DisplayName("@MinimumAge should reject customers younger than 18 years old")
    void shouldRejectCustomerUnderEighteenYearsOld() {
        CreateCustomerRequest request = CreateCustomerRequest.builder()
                .dateOfBirth(LocalDate.now().minusYears(17))
                .build();

        Set<ConstraintViolation<CreateCustomerRequest>> violations =
                validator.validateProperty(request, "dateOfBirth");

        assertThat(violations)
                .isNotEmpty()
                .anySatisfy(v -> assertThat(v.getMessage()).contains("18"));
    }

    @Test
    @DisplayName("@MinimumAge should accept customers 18 years or older")
    void shouldAcceptCustomerAtLeastEighteenYearsOld() {
        CreateCustomerRequest request = CreateCustomerRequest.builder()
                .dateOfBirth(LocalDate.now().minusYears(18))
                .build();

        Set<ConstraintViolation<CreateCustomerRequest>> violations =
                validator.validateProperty(request, "dateOfBirth");

        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("MinimumAgeValidator should allow null (delegated to @NotNull) and reject future dates")
    void shouldDelegateNullToNotNullAndRejectFutureDob() {
        MinimumAgeValidator ageValidator = new MinimumAgeValidator();

        assertThat(ageValidator.isValid(null, null)).isTrue();
        assertThat(ageValidator.isValid(LocalDate.now().plusDays(1), null)).isFalse();
        assertThat(ageValidator.isValid(LocalDate.now().minusYears(25), null)).isTrue();
    }

    @Test
    @DisplayName("TransferRequest should reject sub-paise precision, dual targets, and self-transfers")
    void shouldEnforceTransferMonetaryPrecisionAndCrossFieldRules() {
        TransferRequest precisionViolation = TransferRequest.builder()
                .sourceAccountId(1L)
                .beneficiaryId(2L)
                .amount(new BigDecimal("10.123"))
                .build();

        assertThat(validator.validateProperty(precisionViolation, "amount")).isNotEmpty();

        TransferRequest dualTarget = TransferRequest.builder()
                .sourceAccountId(1L)
                .destinationAccountId(2L)
                .beneficiaryId(3L)
                .amount(new BigDecimal("100.00"))
                .build();

        Set<ConstraintViolation<TransferRequest>> dualViolations = validator.validate(dualTarget);
        assertThat(dualViolations)
                .anySatisfy(v -> assertThat(v.getPropertyPath().toString()).isEqualTo("targetExclusive"));

        TransferRequest selfTransfer = TransferRequest.builder()
                .sourceAccountId(5L)
                .destinationAccountId(5L)
                .amount(new BigDecimal("100.00"))
                .build();

        Set<ConstraintViolation<TransferRequest>> selfViolations = validator.validate(selfTransfer);
        assertThat(selfViolations)
                .anySatisfy(v -> assertThat(v.getPropertyPath().toString()).isEqualTo("notSelfTransfer"));
    }

    @Test
    @DisplayName("TransferRequest valid payload should pass validation and omit @JsonIgnore helper methods")
    void shouldAcceptValidTransferAndIgnoreValidationGettersInJson() {
        TransferRequest valid = TransferRequest.builder()
                .sourceAccountId(1L)
                .beneficiaryId(2L)
                .amount(new BigDecimal("250.50"))
                .remarks("Monthly rent")
                .build();

        assertThat(validator.validate(valid)).isEmpty();

        String json = new ObjectMapper().writeValueAsString(valid);
        assertThat(json)
                .doesNotContain("targetExclusive")
                .doesNotContain("notSelfTransfer");
    }

    @Test
    @DisplayName("Auth DTOs should reject mismatched passwords across register, reset, and change-password")
    void shouldRejectMismatchedPasswordsAcrossAuthDtos() {
        RegisterRequest register = RegisterRequest.builder()
                .fullName("Aarav Sharma")
                .username("aarav_sharma")
                .email("aarav@example.com")
                .phoneNumber("+919876543210")
                .password("StrongPass@123")
                .confirmPassword("Mismatch@123")
                .build();

        assertThat(validator.validate(register))
                .anySatisfy(v -> assertThat(v.getPropertyPath().toString()).isEqualTo("passwordsMatching"));

        ResetPasswordRequest reset = ResetPasswordRequest.builder()
                .token("reset-token-123")
                .newPassword("StrongPass@123")
                .confirmPassword("Mismatch@123")
                .build();

        assertThat(validator.validate(reset))
                .anySatisfy(v -> assertThat(v.getPropertyPath().toString()).isEqualTo("passwordsMatching"));

        ChangePassowordRequest change = ChangePassowordRequest.builder()
                .currentPassword("OldPass@123")
                .newPassword("StrongPass@123")
                .confirmPassword("Mismatch@123")
                .build();

        assertThat(validator.validate(change))
                .anySatisfy(v -> assertThat(v.getPropertyPath().toString()).isEqualTo("passwordsMatching"));
    }
}

