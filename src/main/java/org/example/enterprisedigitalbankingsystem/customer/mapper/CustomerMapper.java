package org.example.enterprisedigitalbankingsystem.customer.mapper;

import org.example.enterprisedigitalbankingsystem.customer.dto.request.CreateCustomerRequest;
import org.example.enterprisedigitalbankingsystem.customer.dto.request.UpdateCustomerRequest;
import org.example.enterprisedigitalbankingsystem.customer.dto.response.CustomerResponse;
import org.example.enterprisedigitalbankingsystem.customer.dto.response.CustomerSummaryResponse;
import org.example.enterprisedigitalbankingsystem.customer.entity.Customer;
import org.springframework.stereotype.Component;

@Component
public class CustomerMapper {

    /**
     * Convert CreateCustomerRequest -> Customer Entity
     */
    public Customer toEntity(CreateCustomerRequest request) {
        Customer customer = new Customer();

        customer.setFirstName(request.getFirstName());
        customer.setLastName(request.getLastName());
        customer.setDateOfBirth(request.getDateOfBirth());
        customer.setPhoneNumber(request.getPhoneNumber());
        customer.setAddress(request.getAddress());
        customer.setCity(request.getCity());
        customer.setState(request.getState());
        customer.setCountry(request.getCountry());
        customer.setPostalCode(request.getPostalCode());

        return customer;
    }

    /**
     * Convert Customer Entity -> CustomerResponse
     */
    public CustomerResponse toResponse(Customer customer) {

        if (customer == null) {
            return null;
        }

        return CustomerResponse.builder()
                .customerId(customer.getId())
                .firstName(customer.getFirstName())
                .lastName(customer.getLastName())
                .dateOfBirth(customer.getDateOfBirth())
                .phoneNumber(customer.getPhoneNumber())
                .address(customer.getAddress())
                .city(customer.getCity())
                .state(customer.getState())
                .country(customer.getCountry())
                .postalCode(customer.getPostalCode())
                .kycStatus(customer.getKycStatus())
                .createdAt(customer.getCreatedAt())
                .build();
    }

    /**
     * Convert Customer Entity -> CustomerSummaryResponse
     */
    public CustomerSummaryResponse toSummaryResponse(Customer customer) {

        if (customer == null) {
            return null;
        }

        return CustomerSummaryResponse.builder()
                .customerId(customer.getId())
                .firstName(customer.getFirstName())
                .lastName(customer.getLastName())
                .phoneNumber(customer.getPhoneNumber())
                .city(customer.getCity())
                .kycStatus(customer.getKycStatus())
                .build();
    }

    /**
     * Update an existing Customer entity from UpdateCustomerRequest
     */
    public void updateEntity(Customer customer, UpdateCustomerRequest request) {

        customer.setFirstName(request.getFirstName());
        customer.setLastName(request.getLastName());
        customer.setDateOfBirth(request.getDateOfBirth());
        customer.setPhoneNumber(request.getPhoneNumber());
        customer.setAddress(request.getAddress());
        customer.setCity(request.getCity());
        customer.setState(request.getState());
        customer.setCountry(request.getCountry());
        customer.setPostalCode(request.getPostalCode());
    }
}