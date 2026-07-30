package org.example.enterprisedigitalbankingsystem.customer.controller;

import org.example.enterprisedigitalbankingsystem.customer.dto.request.CompleteKYCRequest;
import org.example.enterprisedigitalbankingsystem.customer.dto.request.CreateCustomerRequest;
import org.example.enterprisedigitalbankingsystem.customer.dto.request.UpdateCustomerRequest;
import org.example.enterprisedigitalbankingsystem.customer.dto.response.CustomerResponse;
import org.example.enterprisedigitalbankingsystem.customer.dto.response.CustomerSummaryResponse;
import org.example.enterprisedigitalbankingsystem.customer.service.CustomerService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/customers")
public class CustomerController implements CustomerService {

    @Override
    public CustomerResponse createCustomer(CreateCustomerRequest request) {
        return null;
    }

    @Override
    public CustomerResponse getCustomerById(Long customerId) {
        return null;
    }

    @Override
    public CustomerResponse updateCustomer(Long customerId, UpdateCustomerRequest request) {
        return null;
    }

    @Override
    public CustomerResponse getCustomerByUserId(Long userId) {
        return null;
    }

    @Override
    public List<CustomerSummaryResponse> getAllCustomers() {
        return List.of();
    }

    @Override
    public CustomerResponse completeKYC(Long customerId, CompleteKYCRequest request) {
        return null;
    }

    @Override
    public CustomerResponse deleteCustomer(Long customerId) {
        return null;
    }
}
