package org.example.enterprisedigitalbankingsystem.customer.service;

import org.example.enterprisedigitalbankingsystem.common.dto.PagedResponse;
import org.example.enterprisedigitalbankingsystem.customer.dto.request.CompleteKYCRequest;
import org.example.enterprisedigitalbankingsystem.customer.dto.request.CreateCustomerRequest;
import org.example.enterprisedigitalbankingsystem.customer.dto.request.CustomerSearchRequest;
import org.example.enterprisedigitalbankingsystem.customer.dto.request.UpdateCustomerRequest;
import org.example.enterprisedigitalbankingsystem.customer.dto.response.CustomerResponse;
import org.example.enterprisedigitalbankingsystem.customer.dto.response.CustomerSummaryResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface CustomerService {
    CustomerResponse createCustomer(CreateCustomerRequest request);
    CustomerResponse getCustomerById(Long customerId);
    CustomerResponse updateCustomer(Long customerId, UpdateCustomerRequest request);
    CustomerResponse getCustomerByUserId(UUID userId);
    PagedResponse<CustomerSummaryResponse> getAllCustomers(Pageable pageable);
    PagedResponse<CustomerSummaryResponse> searchCustomers(CustomerSearchRequest filter, Pageable pageable);
    CustomerResponse completeKYC(Long customerId, CompleteKYCRequest request);
    void deleteCustomer(Long customerId);
}
