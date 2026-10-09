package org.example.enterprisedigitalbankingsystem.loan.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.enterprisedigitalbankingsystem.loan.dto.request.CreateLoanRequest;
import org.example.enterprisedigitalbankingsystem.loan.dto.response.LoanResponse;
import org.example.enterprisedigitalbankingsystem.loan.service.LoanService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/loans")
@RequiredArgsConstructor
public class LoanController {
    private final LoanService loanService;

    @PostMapping()
    public ResponseEntity<LoanResponse> applyForLoan(@Valid @RequestBody CreateLoanRequest request){
        return  ResponseEntity.ok(loanService.applyForLoan(request));
    }

    @GetMapping("/{loanId}")
    public ResponseEntity<LoanResponse> getLoansById(@Valid @PathVariable Long loanId){
        return ResponseEntity.ok(loanService.getLoanById(loanId));
    }

    @GetMapping("/{customerId}")
    public ResponseEntity<List<LoanResponse>> getLoansByCustomerId(@Valid @PathVariable Long customerId){
        return ResponseEntity.ok(loanService.getLoansByCustomerId(customerId));
    }

    @PatchMapping("/{loanId}")
    public ResponseEntity<LoanResponse> rejectLoan(@Valid @PathVariable Long loanId){
        return ResponseEntity.ok(loanService.approveLoan(loanId));
    }
    @GetMapping("/{loanId}")
    public ResponseEntity<LoanResponse> approve(@Valid @PathVariable Long loanId){
        return ResponseEntity.ok(loanService.rejectLoan(loanId));
    }
}
