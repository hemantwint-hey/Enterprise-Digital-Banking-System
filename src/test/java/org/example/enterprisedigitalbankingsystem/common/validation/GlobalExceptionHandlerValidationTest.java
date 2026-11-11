package org.example.enterprisedigitalbankingsystem.common.validation;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import org.example.enterprisedigitalbankingsystem.exception.ErrorResponse;
import org.example.enterprisedigitalbankingsystem.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.validation.method.ParameterValidationResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import tools.jackson.databind.ObjectMapper;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerValidationTest {

    private GlobalExceptionHandler handler;
    private MockHttpServletRequest request;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        request = new MockHttpServletRequest("POST", "/api/transactions/transfer");
        request.setRequestURI("/api/transactions/transfer");
    }

    @SuppressWarnings("unused")
    private void dummyEndpoint(String accountId) {
    }

    @Test
    @DisplayName("MethodArgumentNotValidException should populate fieldErrors for field and global errors")
    void shouldPopulateFieldAndGlobalErrorsOnMethodArgumentNotValidException() throws NoSuchMethodException {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "transferRequest");
        bindingResult.addError(new FieldError("transferRequest", "amount", "Transfer amount must be positive"));
        bindingResult.addError(new ObjectError("transferRequest", "Provide either destinationAccountId or beneficiaryId"));

        Method method = GlobalExceptionHandlerValidationTest.class.getDeclaredMethod("dummyEndpoint", String.class);
        MethodParameter parameter = new MethodParameter(method, 0);
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(parameter, bindingResult);

        ResponseEntity<ErrorResponse> response = handler.handleValidationException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getError()).isEqualTo("Validation failed");
        assertThat(response.getBody().getPath()).isEqualTo("/api/transactions/transfer");
        assertThat(response.getBody().getFieldErrors())
                .containsEntry("amount", "Transfer amount must be positive")
                .containsEntry("transferRequest", "Provide either destinationAccountId or beneficiaryId");
    }

    @Test
    @DisplayName("HandlerMethodValidationException should extract parameter and cross-parameter errors")
    void shouldExtractParameterAndCrossParameterErrors() throws NoSuchMethodException {
        Method method = GlobalExceptionHandlerValidationTest.class.getDeclaredMethod("dummyEndpoint", String.class);
        MethodParameter methodParameter = mock(MethodParameter.class);
        when(methodParameter.getParameterName()).thenReturn("accountId");

        MessageSourceResolvable paramError = mock(MessageSourceResolvable.class);
        when(paramError.getDefaultMessage()).thenReturn("Account ID must be a positive number");

        ParameterValidationResult paramResult = mock(ParameterValidationResult.class);
        when(paramResult.getMethodParameter()).thenReturn(methodParameter);
        when(paramResult.getResolvableErrors()).thenReturn(List.of(paramError));

        MessageSourceResolvable crossParamError = mock(MessageSourceResolvable.class);
        when(crossParamError.getDefaultMessage()).thenReturn("Cross-parameter rule failed");

        HandlerMethodValidationException ex = mock(HandlerMethodValidationException.class);
        when(ex.getParameterValidationResults()).thenReturn(List.of(paramResult));
        when(ex.getCrossParameterValidationResults()).thenReturn(List.of(crossParamError));

        ResponseEntity<ErrorResponse> response = handler.handleMethodValidationException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getFieldErrors())
                .containsEntry("accountId", "Account ID must be a positive number")
                .containsEntry("request", "Cross-parameter rule failed");
        assertThat(method.getName()).isEqualTo("dummyEndpoint");
    }

    @Test
    @DisplayName("ConstraintViolationException should normalize leaf property names into fieldErrors")
    void shouldExtractLeafPropertyNameOnConstraintViolationException() {
        Path path = mock(Path.class);
        when(path.toString()).thenReturn("getAccountById.id");

        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        when(violation.getPropertyPath()).thenReturn(path);
        when(violation.getMessage()).thenReturn("Account ID must be a positive number");

        ConstraintViolationException ex = new ConstraintViolationException(Set.of(violation));

        ResponseEntity<ErrorResponse> response = handler.handleConstraintViolationException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getError()).isEqualTo("Constraint violation");
        assertThat(response.getBody().getFieldErrors())
                .containsEntry("id", "Account ID must be a positive number");
    }

    @Test
    @DisplayName("Malformed JSON and argument type mismatch should return clean 400 ErrorResponse")
    void shouldReturnBadRequestOnMalformedJsonAndTypeMismatch() {
        HttpMessageNotReadableException jsonEx =
                new HttpMessageNotReadableException("Bad JSON", mock(HttpInputMessage.class));
        ResponseEntity<ErrorResponse> jsonResponse = handler.handleHttpMessageNotReadableException(jsonEx, request);

        assertThat(jsonResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(jsonResponse.getBody()).isNotNull();
        assertThat(jsonResponse.getBody().getError())
                .isEqualTo("Malformed JSON payload or invalid field value/enum format");

        MethodArgumentTypeMismatchException mismatchEx = mock(MethodArgumentTypeMismatchException.class);
        when(mismatchEx.getName()).thenReturn("customerId");
        ResponseEntity<ErrorResponse> mismatchResponse = handler.handleTypeMismatchException(mismatchEx, request);

        assertThat(mismatchResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(mismatchResponse.getBody()).isNotNull();
        assertThat(mismatchResponse.getBody().getError())
                .isEqualTo("Invalid value for parameter 'customerId'");
    }

    @Test
    @DisplayName("ErrorResponse should preserve 4-arg constructor and omit null fieldErrors from JSON")
    void shouldPreserveFourArgConstructorAndOmitNullFieldErrorsFromJson() {
        ObjectMapper objectMapper = new ObjectMapper();

        ErrorResponse plain = new ErrorResponse(LocalDateTime.now(), 404, "Not found", "/api/test");
        assertThat(plain.getFieldErrors()).isNull();
        assertThat(objectMapper.writeValueAsString(plain)).doesNotContain("fieldErrors");

        ErrorResponse withFields = new ErrorResponse(
                LocalDateTime.now(),
                400,
                "Validation failed",
                "/api/test",
                Map.of("amount", "must be positive")
        );
        assertThat(objectMapper.writeValueAsString(withFields)).contains("\"fieldErrors\"");
    }
}

