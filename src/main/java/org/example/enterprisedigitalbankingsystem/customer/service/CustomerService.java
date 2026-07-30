package org.example.enterprisedigitalbankingsystem.customer.service;

import org.example.enterprisedigitalbankingsystem.customer.dto.request.CompleteKYCRequest;
import org.example.enterprisedigitalbankingsystem.customer.dto.request.CreateCustomerRequest;
import org.example.enterprisedigitalbankingsystem.customer.dto.request.UpdateCustomerRequest;
import org.example.enterprisedigitalbankingsystem.customer.dto.response.CustomerResponse;
import org.example.enterprisedigitalbankingsystem.customer.dto.response.CustomerSummaryResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface CustomerService {
    CustomerResponse createCustomer(CreateCustomerRequest request);
    CustomerResponse getCustomerById(Long customerId);
    CustomerResponse updateCustomer(Long customerId, UpdateCustomerRequest request);
    CustomerResponse getCustomerByUserId(Long userId);
    List<CustomerSummaryResponse> getAllCustomers();
    CustomerResponse completeKYC(Long customerId, CompleteKYCRequest request);
    CustomerResponse deleteCustomer(Long customerId);
}
