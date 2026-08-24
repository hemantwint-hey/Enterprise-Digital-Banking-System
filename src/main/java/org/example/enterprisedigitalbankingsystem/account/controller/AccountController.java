package org.example.enterprisedigitalbankingsystem.account.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.enterprisedigitalbankingsystem.account.dto.request.CloseAccountRequest;
import org.example.enterprisedigitalbankingsystem.account.dto.request.CreateAccountRequest;
import org.example.enterprisedigitalbankingsystem.account.dto.request.FreezeAccountRequest;
import org.example.enterprisedigitalbankingsystem.account.dto.request.UpdateAccountRequest;
import org.example.enterprisedigitalbankingsystem.account.dto.response.AccountResponse;
import org.example.enterprisedigitalbankingsystem.account.dto.response.AccountSummaryResponse;
import org.example.enterprisedigitalbankingsystem.account.service.AccountService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
public class AccountController {
        private final AccountService accountService;

        @PostMapping
        public ResponseEntity<AccountResponse> createAccount(@Valid @RequestBody CreateAccountRequest request){
            AccountResponse accountResponse = accountService.createAccount(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(accountResponse);
        }

        @GetMapping("/{accountId}")
        public ResponseEntity<AccountResponse> getAccountById(@PathVariable Long accountId){
            AccountResponse accountResponse = accountService.getAccountById(accountId);
            return  ResponseEntity.ok(accountResponse);
        }
        @GetMapping("/number/{accountNumber}")
        public ResponseEntity<AccountResponse> getAccountByAccountNumber(@PathVariable  String accountNumber){
            AccountResponse response =  accountService.getAccountByAccountNumber(accountNumber);
            return  ResponseEntity.ok(response);
        }
        @GetMapping("/customer/{customerId}")
        public ResponseEntity<List<AccountSummaryResponse>>  getAccountByCustomerId(@PathVariable Long customerId){
            List<AccountSummaryResponse> response =  accountService.getAccountsByCustomerId(customerId);
            return ResponseEntity.ok(response);
        }

        @PutMapping("/{accountId}")
        public ResponseEntity<AccountResponse> updateAccount(
                @PathVariable Long accountId,
                @Valid @RequestBody UpdateAccountRequest request
        ){
            AccountResponse response = accountService.updateAccount(accountId,request);
            return ResponseEntity.ok(response);
        }
        @PatchMapping("/{accountId}/freeze")
        public ResponseEntity<AccountResponse>  freezeAccount(
                @PathVariable Long accountId,
                @Valid @RequestBody FreezeAccountRequest request
        ){
           AccountResponse response = accountService.freezeAccount(accountId, request);
           return ResponseEntity.ok(response);
        }

        @PatchMapping("/{accountId}/close")
        public ResponseEntity<AccountResponse> closeAccount(
                @PathVariable Long accountId,
                @Valid @RequestBody CloseAccountRequest request
        ){
            AccountResponse response = accountService.closeAccount(accountId, request);
            return ResponseEntity.ok(response);
        }
}
