package org.example.enterprisedigitalbankingsystem.emi.controller;

import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.example.enterprisedigitalbankingsystem.emi.dto.response.EmiResponse;
import org.example.enterprisedigitalbankingsystem.emi.service.EmiService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/v1/emis")
@RequiredArgsConstructor
public class EmiController {

    private final EmiService emiService;

    @GetMapping("/loan/{loanId}")
    public ResponseEntity<List<EmiResponse>> getEmisByLoanId(
            @PathVariable @Positive(message = "Loan id must be positive") Long loanId) {
        return ResponseEntity.ok(emiService.getEmisByLoanId(loanId));
    }

    @PatchMapping("/{emiId}/pay")
    public ResponseEntity<EmiResponse> payEmi(
            @PathVariable @Positive(message = "EMI id must be positive") Long emiId) {
        return ResponseEntity.ok(emiService.payEmi(emiId));
    }
}
