package org.example.enterprisedigitalbankingsystem.beneficiary.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.enterprisedigitalbankingsystem.common.dto.PagedResponse;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.example.enterprisedigitalbankingsystem.beneficiary.dto.request.CreateBeneficiaryRequest;
import org.example.enterprisedigitalbankingsystem.beneficiary.dto.request.UpdateBeneficiaryRequest;
import org.example.enterprisedigitalbankingsystem.beneficiary.dto.response.BeneficiaryResponse;
import org.example.enterprisedigitalbankingsystem.beneficiary.service.BeneficiaryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@Tag(name = "Beneficiaries", description = "Beneficiary management and activation APIs")
@RestController
@RequestMapping("/api/v1/beneficiaries")
@RequiredArgsConstructor
public class BeneficiaryController {

    private final BeneficiaryService beneficiaryService;

    @PostMapping
    public ResponseEntity<BeneficiaryResponse> addBeneficiary(@Valid @RequestBody CreateBeneficiaryRequest request) {
        BeneficiaryResponse response = beneficiaryService.addBeneficiary(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{beneficiaryId}")
    public ResponseEntity<BeneficiaryResponse> getBeneficiaryById(
            @PathVariable @Positive(message = "Beneficiary id must be positive") Long beneficiaryId) {
        BeneficiaryResponse response = beneficiaryService.getBeneficiaryById(beneficiaryId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/customer/{customerId}")
    @Operation(summary = "Get paginated beneficiaries for a customer")
    public ResponseEntity<PagedResponse<BeneficiaryResponse>> getBeneficiariesByCustomerId(
            @PathVariable @Positive(message = "Customer id must be positive") Long customerId,
            @ParameterObject @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(beneficiaryService.getBeneficiariesByCustomerId(customerId, pageable));
    }

    @PutMapping("/{beneficiaryId}")
    public ResponseEntity<BeneficiaryResponse> updateBeneficiary(
            @PathVariable @Positive(message = "Beneficiary id must be positive") Long beneficiaryId,
            @Valid @RequestBody UpdateBeneficiaryRequest request
    ) {
        BeneficiaryResponse response = beneficiaryService.updateBeneficiary(beneficiaryId, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{beneficiaryId}/activate")
    public ResponseEntity<BeneficiaryResponse> activateBeneficiary(
            @PathVariable @Positive(message = "Beneficiary id must be positive") Long beneficiaryId) {
        BeneficiaryResponse response = beneficiaryService.activateBeneficiary(beneficiaryId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{beneficiaryId}")
    public ResponseEntity<Void> deleteBeneficiary(
            @PathVariable @Positive(message = "Beneficiary id must be positive") Long beneficiaryId) {
        beneficiaryService.deleteBeneficiary(beneficiaryId);
        return ResponseEntity.noContent().build();
    }
}
