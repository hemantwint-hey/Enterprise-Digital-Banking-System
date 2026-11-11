package org.example.enterprisedigitalbankingsystem.loan.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.example.enterprisedigitalbankingsystem.loan.dto.request.CreateLoanRequest;
import org.example.enterprisedigitalbankingsystem.loan.dto.response.LoanResponse;
import org.example.enterprisedigitalbankingsystem.loan.service.LoanService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/v1/loans")
@RequiredArgsConstructor
public class LoanController {
    private final LoanService loanService;

    @PostMapping
    public ResponseEntity<LoanResponse> applyForLoan(@Valid @RequestBody CreateLoanRequest request){
        return ResponseEntity.status(HttpStatus.CREATED).body(loanService.applyForLoan(request));
    }

    @GetMapping("/{loanId}")
    public ResponseEntity<LoanResponse> getLoanById(
            @PathVariable @Positive(message = "Loan id must be positive") Long loanId){
        return ResponseEntity.ok(loanService.getLoanById(loanId));
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<LoanResponse>> getLoansByCustomerId(
            @PathVariable @Positive(message = "Customer id must be positive") Long customerId){
        return ResponseEntity.ok(loanService.getLoansByCustomerId(customerId));
    }

    @PatchMapping("/{loanId}/approve")
    public ResponseEntity<LoanResponse> approveLoan(
            @PathVariable @Positive(message = "Loan id must be positive") Long loanId){
        return ResponseEntity.ok(loanService.approveLoan(loanId));
    }

    @PatchMapping("/{loanId}/reject")
    public ResponseEntity<LoanResponse> rejectLoan(
            @PathVariable @Positive(message = "Loan id must be positive") Long loanId){
        return ResponseEntity.ok(loanService.rejectLoan(loanId));
    }
}
