package org.example.enterprisedigitalbankingsystem.transaction.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.enterprisedigitalbankingsystem.transaction.dto.request.DepositRequest;
import org.example.enterprisedigitalbankingsystem.transaction.dto.request.TransferRequest;
import org.example.enterprisedigitalbankingsystem.transaction.dto.request.WithdrawRequest;
import org.example.enterprisedigitalbankingsystem.transaction.dto.response.TransactionResponse;
import org.example.enterprisedigitalbankingsystem.transaction.dto.response.TransactionSummaryResponse;
import org.example.enterprisedigitalbankingsystem.transaction.service.TransactionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public ResponseEntity<TransactionResponse> getTransactionById(@PathVariable Long transactionId) {
        TransactionResponse response = transactionService.getTransactionById(transactionId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/reference/{transactionReference}")
    public ResponseEntity<TransactionResponse> getTransactionByReference(@PathVariable String transactionReference) {
       TransactionResponse response = transactionService.getTransactionByReference(transactionReference);
       return ResponseEntity.ok(response);
    }

    @GetMapping("/account/{accountId}/history")
    public ResponseEntity<List<TransactionSummaryResponse>> getTransactionHistory(@PathVariable Long accountId) {
      List<TransactionSummaryResponse> response = transactionService.getTransactionHistory(accountId);
      return ResponseEntity.ok(response);
    }
}
