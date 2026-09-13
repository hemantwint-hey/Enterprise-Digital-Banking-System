package org.example.enterprisedigitalbankingsystem.beneficiary.service;

import org.example.enterprisedigitalbankingsystem.beneficiary.dto.request.CreateBeneficiaryRequest;
import org.example.enterprisedigitalbankingsystem.beneficiary.dto.request.UpdateBeneficiaryRequest;
import org.example.enterprisedigitalbankingsystem.beneficiary.dto.response.BeneficiaryResponse;

import java.util.List;

public interface BeneficiaryService {
    BeneficiaryResponse addBeneficiary(CreateBeneficiaryRequest request);

    BeneficiaryResponse getBeneficiaryById(Long beneficiaryId);

    List<BeneficiaryResponse> getBeneficiariesByCustomerId(Long customerId);

    BeneficiaryResponse updateBeneficiary(Long beneficiaryId, UpdateBeneficiaryRequest request);

    BeneficiaryResponse activateBeneficiary(Long beneficiaryId);

    void deleteBeneficiary(Long beneficiaryId);
}
