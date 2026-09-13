package org.example.enterprisedigitalbankingsystem.beneficiary.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.enterprisedigitalbankingsystem.beneficiary.dto.request.CreateBeneficiaryRequest;
import org.example.enterprisedigitalbankingsystem.beneficiary.dto.request.UpdateBeneficiaryRequest;
import org.example.enterprisedigitalbankingsystem.beneficiary.dto.response.BeneficiaryResponse;
import org.example.enterprisedigitalbankingsystem.beneficiary.service.BeneficiaryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public ResponseEntity<BeneficiaryResponse> getBeneficiaryById(@PathVariable Long beneficiaryId) {
        BeneficiaryResponse response = beneficiaryService.getBeneficiaryById(beneficiaryId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<BeneficiaryResponse>> getBeneficiariesByCustomerId(@PathVariable Long customerId) {
        List<BeneficiaryResponse> response = beneficiaryService.getBeneficiariesByCustomerId(customerId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{beneficiaryId}")
    public ResponseEntity<BeneficiaryResponse> updateBeneficiary(
            @PathVariable Long beneficiaryId,
            @Valid @RequestBody UpdateBeneficiaryRequest request
    ) {
        BeneficiaryResponse response = beneficiaryService.updateBeneficiary(beneficiaryId, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{beneficiaryId}/activate")
    public ResponseEntity<BeneficiaryResponse> activateBeneficiary(@PathVariable Long beneficiaryId) {
        BeneficiaryResponse response = beneficiaryService.activateBeneficiary(beneficiaryId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{beneficiaryId}")
    public ResponseEntity<Void> deleteBeneficiary(@PathVariable Long beneficiaryId) {
        beneficiaryService.deleteBeneficiary(beneficiaryId);
        return ResponseEntity.noContent().build();
    }
}
