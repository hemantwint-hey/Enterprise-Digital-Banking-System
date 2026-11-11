package org.example.enterprisedigitalbankingsystem.transaction.service;

import org.example.enterprisedigitalbankingsystem.common.dto.PagedResponse;
import org.example.enterprisedigitalbankingsystem.transaction.dto.request.DepositRequest;
import org.example.enterprisedigitalbankingsystem.transaction.dto.request.TransactionSearchRequest;
import org.example.enterprisedigitalbankingsystem.transaction.dto.request.TransferRequest;
import org.example.enterprisedigitalbankingsystem.transaction.dto.request.WithdrawRequest;
import org.example.enterprisedigitalbankingsystem.transaction.dto.response.TransactionResponse;
import org.example.enterprisedigitalbankingsystem.transaction.dto.response.TransactionSummaryResponse;
import org.springframework.data.domain.Pageable;

public interface TransactionService {
    TransactionResponse deposit(DepositRequest request);
    TransactionResponse withdraw(WithdrawRequest request);
    TransactionResponse transfer(TransferRequest request);
    TransactionResponse getTransactionById(Long transactionId);
    TransactionResponse getTransactionByReference(String transactionReference);
    PagedResponse<TransactionSummaryResponse> getTransactionHistory(Long accountId, Pageable pageable);
    PagedResponse<TransactionSummaryResponse> searchTransactions(TransactionSearchRequest filter, Pageable pageable);
}
