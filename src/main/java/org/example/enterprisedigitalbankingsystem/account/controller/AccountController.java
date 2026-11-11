package org.example.enterprisedigitalbankingsystem.account.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.enterprisedigitalbankingsystem.account.dto.request.AccountSearchRequest;
import org.example.enterprisedigitalbankingsystem.common.dto.PagedResponse;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.ModelAttribute;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.example.enterprisedigitalbankingsystem.account.dto.request.CloseAccountRequest;
import org.example.enterprisedigitalbankingsystem.account.dto.request.CreateAccountRequest;
import org.example.enterprisedigitalbankingsystem.account.dto.request.FreezeAccountRequest;
import org.example.enterprisedigitalbankingsystem.account.dto.request.UpdateAccountRequest;
import org.example.enterprisedigitalbankingsystem.account.dto.response.AccountResponse;
import org.example.enterprisedigitalbankingsystem.account.dto.response.AccountSummaryResponse;
import org.example.enterprisedigitalbankingsystem.account.service.AccountService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@Tag(name = "Accounts", description = "Bank account creation, lifecycle management, and dynamic search APIs")
@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
public class AccountController {
        private final AccountService accountService;

        @PostMapping
        public ResponseEntity<AccountResponse> createAccount(@Valid @RequestBody CreateAccountRequest request){
            AccountResponse accountResponse = accountService.createAccount(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(accountResponse);
        }

        @GetMapping("/{accountId}")
        public ResponseEntity<AccountResponse> getAccountById(
                @PathVariable @Positive(message = "Account id must be positive") Long accountId){
            AccountResponse accountResponse = accountService.getAccountById(accountId);
            return  ResponseEntity.ok(accountResponse);
        }
        @GetMapping("/number/{accountNumber}")
        public ResponseEntity<AccountResponse> getAccountByAccountNumber(
                @PathVariable @NotBlank(message = "Account number is required")
                @Size(max = 30, message = "Account number cannot exceed 30 characters") String accountNumber){
            AccountResponse response =  accountService.getAccountByAccountNumber(accountNumber);
            return  ResponseEntity.ok(response);
        }
        @GetMapping("/customer/{customerId}")
        @Operation(summary = "Get paginated accounts for a customer")
        public ResponseEntity<PagedResponse<AccountSummaryResponse>> getAccountByCustomerId(
                @PathVariable @Positive(message = "Customer id must be positive") Long customerId,
                @ParameterObject @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable){
            return ResponseEntity.ok(accountService.getAccountsByCustomerId(customerId, pageable));
        }

        @GetMapping("/search")
        @Operation(summary = "Search accounts dynamically by customer, account number, type, status, branch, and balance range")
        public ResponseEntity<PagedResponse<AccountSummaryResponse>> searchAccounts(
                @ParameterObject @Valid @ModelAttribute AccountSearchRequest filter,
                @ParameterObject @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable){
            return ResponseEntity.ok(accountService.searchAccounts(filter, pageable));
        }

        @PutMapping("/{accountId}")
        public ResponseEntity<AccountResponse> updateAccount(
                @PathVariable @Positive(message = "Account id must be positive") Long accountId,
                @Valid @RequestBody UpdateAccountRequest request
        ){
            AccountResponse response = accountService.updateAccount(accountId,request);
            return ResponseEntity.ok(response);
        }
        @PatchMapping("/{accountId}/freeze")
        public ResponseEntity<AccountResponse>  freezeAccount(
                @PathVariable @Positive(message = "Account id must be positive") Long accountId,
                @Valid @RequestBody FreezeAccountRequest request
        ){
           AccountResponse response = accountService.freezeAccount(accountId, request);
           return ResponseEntity.ok(response);
        }

        @PatchMapping("/{accountId}/close")
        public ResponseEntity<AccountResponse> closeAccount(
                @PathVariable @Positive(message = "Account id must be positive") Long accountId,
                @Valid @RequestBody CloseAccountRequest request
        ){
            AccountResponse response = accountService.closeAccount(accountId, request);
            return ResponseEntity.ok(response);
        }
}
