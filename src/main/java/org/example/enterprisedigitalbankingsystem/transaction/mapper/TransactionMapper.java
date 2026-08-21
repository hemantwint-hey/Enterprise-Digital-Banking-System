package org.example.enterprisedigitalbankingsystem.transaction.mapper;

import org.example.enterprisedigitalbankingsystem.account.entity.Account;
import org.example.enterprisedigitalbankingsystem.transaction.dto.response.TransactionResponse;
import org.example.enterprisedigitalbankingsystem.transaction.dto.response.TransactionSummaryResponse;
import org.example.enterprisedigitalbankingsystem.transaction.entity.Transaction;
import org.springframework.stereotype.Component;

@Component
public class TransactionMapper {

    public TransactionResponse toResponse(Transaction transaction) {
        if (transaction == null) {
            return null;
        }

        Account sourceAccount = transaction.getSourceAccount();
        Account destinationAccount = transaction.getDestinationAccount();

        return TransactionResponse.builder()
                .transactionId(transaction.getId())
                .transactionReference(transaction.getTransactionReference())
                .sourceAccountId(sourceAccount != null ? sourceAccount.getId() : null)
                .sourceAccountNumber(sourceAccount != null ? sourceAccount.getAccountNumber() : null)
                .destinationAccountId(destinationAccount != null ? destinationAccount.getId() : null)
                .destinationAccountNumber(destinationAccount != null ? destinationAccount.getAccountNumber() : null)
                .amount(transaction.getAmount())
                .transactionType(transaction.getTransactionType())
                .transactionStatus(transaction.getTransactionStatus())
                .balanceAfterTransaction(transaction.getBalanceAfterTransaction())
                .remarks(transaction.getRemarks())
                .createdAt(transaction.getCreatedAt())
                .build();
    }

    public TransactionSummaryResponse toSummaryResponse(Transaction transaction) {
        if (transaction == null) {
            return null;
        }

        return TransactionSummaryResponse.builder()
                .transactionId(transaction.getId())
                .transactionReference(transaction.getTransactionReference())
                .amount(transaction.getAmount())
                .transactionType(transaction.getTransactionType())
                .transactionStatus(transaction.getTransactionStatus())
                .createdAt(transaction.getCreatedAt())
                .build();
    }
}
