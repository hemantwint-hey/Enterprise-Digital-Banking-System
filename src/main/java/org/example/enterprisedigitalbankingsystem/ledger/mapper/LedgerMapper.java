package org.example.enterprisedigitalbankingsystem.ledger.mapper;

import org.example.enterprisedigitalbankingsystem.ledger.dto.response.LedgerEntryResponse;
import org.example.enterprisedigitalbankingsystem.ledger.entity.LedgerEntry;
import org.springframework.stereotype.Component;

@Component
public class LedgerMapper {

    public LedgerEntryResponse toResponse(LedgerEntry ledgerEntry) {
        if (ledgerEntry == null) {
            return null;
        }

        return LedgerEntryResponse.builder()
                .id(ledgerEntry.getId())
                .transactionId(ledgerEntry.getTransaction().getId())
                .accountId(ledgerEntry.getAccount().getId())
                .accountNumber(ledgerEntry.getAccount().getAccountNumber())
                .entryType(ledgerEntry.getEntryType())
                .amount(ledgerEntry.getAmount())
                .balanceAfterEntry(ledgerEntry.getBalanceAfterEntry())
                .narration(ledgerEntry.getNarration())
                .createdAt(ledgerEntry.getCreatedAt())
                .build();
    }
}
