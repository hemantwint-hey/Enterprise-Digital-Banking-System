package org.example.enterprisedigitalbankingsystem.emi.mapper;

import org.example.enterprisedigitalbankingsystem.emi.dto.response.EmiResponse;
import org.example.enterprisedigitalbankingsystem.emi.entity.Emi;
import org.springframework.stereotype.Component;

@Component
public class EmiMapper {
    public EmiResponse toResponse(Emi emi) {
        if (emi == null) return null;
        return EmiResponse.builder()
                .id(emi.getId())
                .loanId(emi.getLoan().getId())
                .installmentNumber(emi.getInstallmentNumber())
                .dueDate(emi.getDueDate())
                .amount(emi.getAmount())
                .status(emi.getStatus())
                .paidAt(emi.getPaidAt())
                .build();
    }
}
