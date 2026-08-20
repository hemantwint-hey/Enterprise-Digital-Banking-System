package org.example.enterprisedigitalbankingsystem.account.mapper;

import org.example.enterprisedigitalbankingsystem.account.dto.response.AccountResponse;
import org.example.enterprisedigitalbankingsystem.account.dto.response.AccountSummaryResponse;
import org.example.enterprisedigitalbankingsystem.account.entity.Account;
import org.example.enterprisedigitalbankingsystem.account.dto.request.UpdateAccountRequest;
import org.example.enterprisedigitalbankingsystem.customer.entity.Customer;
import org.springframework.stereotype.Component;

@Component
public class AccountMapper {

    public AccountResponse toResponse(Account account) {
        if (account == null) {
            return null;
        }

        Customer customer = account.getCustomer();

        return AccountResponse.builder()
                .accountId(account.getId())
                .accountNumber(account.getAccountNumber())
                .customerId(customer != null ? customer.getId() : null)
                .customerName(customer != null ? buildCustomerName(customer) : null)
                .accountHolderName(account.getAccountHolderName())
                .branch(account.getBranch())
                .balance(account.getBalance())
                .accountType(account.getAccountType())
                .accountStatus(account.getAccountStatus())
                .createdAt(account.getCreatedAt())
                .updatedAt(account.getUpdatedAt())
                .build();
    }

    public AccountSummaryResponse toSummaryResponse(Account account) {
        if (account == null) {
            return null;
        }

        return AccountSummaryResponse.builder()
                .accountId(account.getId())
                .accountNumber(account.getAccountNumber())
                .accountHolderName(account.getAccountHolderName())
                .balance(account.getBalance())
                .accountType(account.getAccountType())
                .accountStatus(account.getAccountStatus())
                .build();
    }

    public void updateEntity(Account account, UpdateAccountRequest request) {
        account.setBranch(request.getBranch());
        account.setAccountType(request.getAccountType());
    }

    public String buildCustomerName(Customer customer) {
        return customer.getFirstName() + " " + customer.getLastName();
    }
}
