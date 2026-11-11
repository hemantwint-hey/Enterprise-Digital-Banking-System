package org.example.enterprisedigitalbankingsystem.ledger.controller;

import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.example.enterprisedigitalbankingsystem.ledger.dto.response.LedgerEntryResponse;
import org.example.enterprisedigitalbankingsystem.ledger.service.LedgerService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/v1/ledger")
@RequiredArgsConstructor
public class LedgerController {

    private final LedgerService ledgerService;

    @GetMapping("/account/{accountId}")
    public ResponseEntity<List<LedgerEntryResponse>> getLedgerByAccount(
            @PathVariable @Positive(message = "Account id must be positive") Long accountId) {
        List<LedgerEntryResponse> response = ledgerService.getLedgerByAccount(accountId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/transaction/{transactionId}")
    public ResponseEntity<List<LedgerEntryResponse>> getLedgerByTransaction(
            @PathVariable @Positive(message = "Transaction id must be positive") Long transactionId) {
        List<LedgerEntryResponse> response = ledgerService.getLedgerByTransaction(transactionId);
        return ResponseEntity.ok(response);
    }
}
