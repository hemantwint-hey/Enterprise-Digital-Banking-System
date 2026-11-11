package org.example.enterprisedigitalbankingsystem.emi.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.enterprisedigitalbankingsystem.common.dto.PagedResponse;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;

import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.example.enterprisedigitalbankingsystem.emi.dto.response.EmiResponse;
import org.example.enterprisedigitalbankingsystem.emi.service.EmiService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@Tag(name = "EMIs", description = "EMI installment schedule and repayment APIs")
@RestController
@RequestMapping("/api/v1/emis")
@RequiredArgsConstructor
public class EmiController {

    private final EmiService emiService;

    @GetMapping("/loan/{loanId}")
    @Operation(summary = "Get paginated EMI schedule for a loan")
    public ResponseEntity<PagedResponse<EmiResponse>> getEmisByLoanId(
            @PathVariable @Positive(message = "Loan id must be positive") Long loanId,
            @ParameterObject @PageableDefault(size = 20, sort = "installmentNumber", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(emiService.getEmisByLoanId(loanId, pageable));
    }

    @PatchMapping("/{emiId}/pay")
    public ResponseEntity<EmiResponse> payEmi(
            @PathVariable @Positive(message = "EMI id must be positive") Long emiId) {
        return ResponseEntity.ok(emiService.payEmi(emiId));
    }
}
