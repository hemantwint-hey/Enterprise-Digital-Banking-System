package org.example.enterprisedigitalbankingsystem.ledger.controller;

import lombok.RequiredArgsConstructor;
import org.example.enterprisedigitalbankingsystem.ledger.dto.response.LedgerEntryResponse;
import org.example.enterprisedigitalbankingsystem.ledger.service.LedgerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/ledger")
@RequiredArgsConstructor
public class LedgerController {

    private final LedgerService ledgerService;

    @GetMapping("/account/{accountId}")
    public ResponseEntity<List<LedgerEntryResponse>> getLedgerByAccount(@PathVariable Long accountId) {
        List<LedgerEntryResponse> response = ledgerService.getLedgerByAccount(accountId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/transaction/{transactionId}")
    public ResponseEntity<List<LedgerEntryResponse>> getLedgerByTransaction(@PathVariable Long transactionId) {
        List<LedgerEntryResponse> response = ledgerService.getLedgerByTransaction(transactionId);
        return ResponseEntity.ok(response);
    }
}
