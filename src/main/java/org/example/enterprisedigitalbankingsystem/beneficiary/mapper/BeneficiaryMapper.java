package org.example.enterprisedigitalbankingsystem.beneficiary.mapper;

import org.example.enterprisedigitalbankingsystem.account.entity.Account;
import org.example.enterprisedigitalbankingsystem.beneficiary.dto.response.BeneficiaryResponse;
import org.example.enterprisedigitalbankingsystem.beneficiary.entity.Beneficiary;
import org.springframework.stereotype.Component;

@Component
public class BeneficiaryMapper {

    public BeneficiaryResponse toResponse(Beneficiary beneficiary) {
        if (beneficiary == null) {
            return null;
        }

        Account account = beneficiary.getBeneficiaryAccount();

        return BeneficiaryResponse.builder()
                .id(beneficiary.getId())
                .nickName(beneficiary.getNickName())
                .accountId(account != null ? account.getId() : null)
                .status(beneficiary.getStatus())
                .createdAt(beneficiary.getCreatedAt())
                .build();
    }
}
