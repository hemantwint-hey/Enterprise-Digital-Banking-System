package org.example.enterprisedigitalbankingsystem.account.service;

import org.example.enterprisedigitalbankingsystem.account.dto.request.CloseAccountRequest;
import org.example.enterprisedigitalbankingsystem.account.dto.request.CreateAccountRequest;
import org.example.enterprisedigitalbankingsystem.account.dto.request.FreezeAccountRequest;
import org.example.enterprisedigitalbankingsystem.account.dto.request.UpdateAccountRequest;
import org.example.enterprisedigitalbankingsystem.account.dto.response.AccountResponse;
import org.example.enterprisedigitalbankingsystem.account.dto.response.AccountSummaryResponse;

import java.util.List;

public interface AccountService {
    AccountResponse createAccount(CreateAccountRequest request);
    AccountResponse getAccountById(Long accountId);
    AccountResponse getAccountByAccountNumber(String accountNumber);
    List<AccountSummaryResponse> getAccountsByCustomerId(Long customerId);
    AccountResponse updateAccount(Long accountId, UpdateAccountRequest request);
    AccountResponse freezeAccount(Long accountId, FreezeAccountRequest request);
    AccountResponse closeAccount(Long accountId, CloseAccountRequest request);
}
