package org.example.enterprisedigitalbankingsystem.beneficiary.service;

import org.example.enterprisedigitalbankingsystem.beneficiary.dto.request.CreateBeneficiaryRequest;
import org.example.enterprisedigitalbankingsystem.beneficiary.dto.request.UpdateBeneficiaryRequest;
import org.example.enterprisedigitalbankingsystem.beneficiary.dto.response.BeneficiaryResponse;
import org.example.enterprisedigitalbankingsystem.common.dto.PagedResponse;
import org.springframework.data.domain.Pageable;

public interface BeneficiaryService {
    BeneficiaryResponse addBeneficiary(CreateBeneficiaryRequest request);

    BeneficiaryResponse getBeneficiaryById(Long beneficiaryId);

    PagedResponse<BeneficiaryResponse> getBeneficiariesByCustomerId(Long customerId, Pageable pageable);

    BeneficiaryResponse updateBeneficiary(Long beneficiaryId, UpdateBeneficiaryRequest request);

    BeneficiaryResponse activateBeneficiary(Long beneficiaryId);

    void deleteBeneficiary(Long beneficiaryId);
}
