package org.example.enterprisedigitalbankingsystem.customer.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.enterprisedigitalbankingsystem.auth.entity.User;
import org.example.enterprisedigitalbankingsystem.auth.repository.UserRepository;
import org.example.enterprisedigitalbankingsystem.customer.dto.request.CompleteKYCRequest;
import org.example.enterprisedigitalbankingsystem.customer.dto.request.CreateCustomerRequest;
import org.example.enterprisedigitalbankingsystem.customer.dto.request.UpdateCustomerRequest;
import org.example.enterprisedigitalbankingsystem.customer.dto.response.CustomerResponse;
import org.example.enterprisedigitalbankingsystem.customer.dto.response.CustomerSummaryResponse;
import org.example.enterprisedigitalbankingsystem.customer.entity.Customer;
import org.example.enterprisedigitalbankingsystem.customer.mapper.CustomerMapper;
import org.example.enterprisedigitalbankingsystem.customer.repository.CustomerRepository;
import org.example.enterprisedigitalbankingsystem.customer.service.CustomerService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {
    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    private final CustomerMapper customerMapper;

    @Override
    public CustomerResponse createCustomer(CreateCustomerRequest request) {
        Customer user  = customerRepository.findByUser(request.get)
        Customer customer = customerMapper.toEntity(request);
        customerRepository.save(customer);

        return new CustomerResponse();

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
    public void deleteCustomer(Long customerId) {

    }
}
