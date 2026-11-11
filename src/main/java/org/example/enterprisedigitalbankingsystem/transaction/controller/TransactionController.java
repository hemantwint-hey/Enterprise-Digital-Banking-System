package org.example.enterprisedigitalbankingsystem.transaction.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.enterprisedigitalbankingsystem.common.dto.PagedResponse;
import org.example.enterprisedigitalbankingsystem.transaction.dto.request.TransactionSearchRequest;
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
import org.example.enterprisedigitalbankingsystem.transaction.dto.request.DepositRequest;
import org.example.enterprisedigitalbankingsystem.transaction.dto.request.TransferRequest;
import org.example.enterprisedigitalbankingsystem.transaction.dto.request.WithdrawRequest;
import org.example.enterprisedigitalbankingsystem.transaction.dto.response.TransactionResponse;
import org.example.enterprisedigitalbankingsystem.transaction.dto.response.TransactionSummaryResponse;
import org.example.enterprisedigitalbankingsystem.transaction.service.TransactionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@Tag(name = "Transactions", description = "Deposit, withdrawal, transfer, history, and dynamic search APIs")
@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping("/deposit")
    public ResponseEntity<TransactionResponse> deposit(@Valid @RequestBody DepositRequest request) {
        TransactionResponse response = transactionService.deposit(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/withdraw")
    public ResponseEntity<TransactionResponse> withdraw(@Valid @RequestBody WithdrawRequest request) {
        TransactionResponse response = transactionService.withdraw(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/transfer")
    public ResponseEntity<TransactionResponse> transfer(@Valid @RequestBody TransferRequest request) {
        TransactionResponse response = transactionService.transfer(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{transactionId}")
    public ResponseEntity<TransactionResponse> getTransactionById(
            @PathVariable @Positive(message = "Transaction id must be positive") Long transactionId) {
        TransactionResponse response = transactionService.getTransactionById(transactionId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/reference/{transactionReference}")
    public ResponseEntity<TransactionResponse> getTransactionByReference(
            @PathVariable @NotBlank(message = "Transaction reference is required")
            @Size(max = 64, message = "Transaction reference cannot exceed 64 characters") String transactionReference) {
       TransactionResponse response = transactionService.getTransactionByReference(transactionReference);
       return ResponseEntity.ok(response);
    }

    @GetMapping("/account/{accountId}/history")
    @Operation(summary = "Get paginated transaction history for an account")
    public ResponseEntity<PagedResponse<TransactionSummaryResponse>> getTransactionHistory(
            @PathVariable @Positive(message = "Account id must be positive") Long accountId,
            @ParameterObject @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(transactionService.getTransactionHistory(accountId, pageable));
    }

    @GetMapping("/search")
    @Operation(summary = "Search transactions dynamically by account, reference, type, status, amount, date range, and remarks")
    public ResponseEntity<PagedResponse<TransactionSummaryResponse>> searchTransactions(
            @ParameterObject @Valid @ModelAttribute TransactionSearchRequest filter,
            @ParameterObject @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(transactionService.searchTransactions(filter, pageable));
    }
}
