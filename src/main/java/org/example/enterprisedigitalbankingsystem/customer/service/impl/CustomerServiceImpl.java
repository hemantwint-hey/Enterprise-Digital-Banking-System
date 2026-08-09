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
import org.example.enterprisedigitalbankingsystem.customer.entity.KYCStatus;
import org.example.enterprisedigitalbankingsystem.customer.mapper.CustomerMapper;
import org.example.enterprisedigitalbankingsystem.customer.repository.CustomerRepository;
import org.example.enterprisedigitalbankingsystem.customer.service.CustomerService;
import org.example.enterprisedigitalbankingsystem.exception.ResourceNotFoundException;
import org.example.enterprisedigitalbankingsystem.exception.UserAlreadyExistsException;
import org.example.enterprisedigitalbankingsystem.exception.UserNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    private final CustomerMapper customerMapper;

    @Override
    public CustomerResponse createCustomer(CreateCustomerRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + request.getEmail()));

        if (customerRepository.findByUserUserId(user.getUserId()).isPresent()) {
            throw new UserAlreadyExistsException("Customer profile already exists for this user");
        }

        if (customerRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new UserAlreadyExistsException("Customer profile already exists with phone number: " + request.getPhoneNumber());
        }

        Customer customer = customerMapper.toEntity(request);
        customer.setUser(user);
        customer.setKycStatus(KYCStatus.PENDING);

        return customerMapper.toResponse(customerRepository.save(customer));
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerResponse getCustomerById(Long customerId) {
        return customerMapper.toResponse(findCustomerById(customerId));
    }

    @Override
    public CustomerResponse updateCustomer(Long customerId, UpdateCustomerRequest request) {
        Customer customer = findCustomerById(customerId);

        customerRepository.findByPhoneNumber(request.getPhoneNumber())
                .filter(existingCustomer -> !existingCustomer.getId().equals(customerId))
                .ifPresent(existingCustomer -> {
                    throw new UserAlreadyExistsException("Customer profile already exists with phone number: " + request.getPhoneNumber());
                });

        customerMapper.updateEntity(customer, request);
        return customerMapper.toResponse(customerRepository.save(customer));
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerResponse getCustomerByUserId(UUID userId) {
        Customer customer = customerRepository.findByUserUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found for user id: " + userId));

        return customerMapper.toResponse(customer);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CustomerSummaryResponse> getAllCustomers() {
        return customerRepository.findAll()
                .stream()
                .map(customerMapper::toSummaryResponse)
                .toList();
    }

    @Override
    public CustomerResponse completeKYC(Long customerId, CompleteKYCRequest request) {
        Customer customer = findCustomerById(customerId);

        customerRepository.findByPanNumber(request.getPanNumber())
                .filter(existingCustomer -> !existingCustomer.getId().equals(customerId))
                .ifPresent(existingCustomer -> {
                    throw new UserAlreadyExistsException("PAN number is already linked with another customer");
                });

        customerRepository.findByAadhaarNumber(request.getAadhaarNumber())
                .filter(existingCustomer -> !existingCustomer.getId().equals(customerId))
                .ifPresent(existingCustomer -> {
                    throw new UserAlreadyExistsException("Aadhaar number is already linked with another customer");
                });

        customer.setPanNumber(request.getPanNumber());
        customer.setAadhaarNumber(request.getAadhaarNumber());
        customer.setKycStatus(KYCStatus.VERIFIED);
        customer.getUser().setKycVerified(true);

        return customerMapper.toResponse(customerRepository.save(customer));
    }

    @Override
    public void deleteCustomer(Long customerId) {
        Customer customer = findCustomerById(customerId);
        customer.getUser().setKycVerified(false);
        customerRepository.delete(customer);
    }

    private Customer findCustomerById(Long customerId) {
        return customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + customerId));
    }
}
