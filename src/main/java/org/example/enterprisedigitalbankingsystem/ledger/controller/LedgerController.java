package org.example.enterprisedigitalbankingsystem.ledger.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.enterprisedigitalbankingsystem.common.dto.PagedResponse;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;

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
@Tag(name = "Ledger", description = "Double-entry accounting ledger entry APIs")
@RestController
@RequestMapping("/api/v1/ledger")
@RequiredArgsConstructor
public class LedgerController {

    private final LedgerService ledgerService;

    @GetMapping("/account/{accountId}")
    @Operation(summary = "Get paginated ledger entries for an account")
    public ResponseEntity<PagedResponse<LedgerEntryResponse>> getLedgerByAccount(
            @PathVariable @Positive(message = "Account id must be positive") Long accountId,
            @ParameterObject @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(ledgerService.getLedgerByAccount(accountId, pageable));
    }

    @GetMapping("/transaction/{transactionId}")
    public ResponseEntity<List<LedgerEntryResponse>> getLedgerByTransaction(
            @PathVariable @Positive(message = "Transaction id must be positive") Long transactionId) {
        List<LedgerEntryResponse> response = ledgerService.getLedgerByTransaction(transactionId);
        return ResponseEntity.ok(response);
    }
}
