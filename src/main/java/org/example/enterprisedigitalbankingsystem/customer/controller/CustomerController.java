package org.example.enterprisedigitalbankingsystem.customer.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.enterprisedigitalbankingsystem.customer.dto.request.CompleteKYCRequest;
import org.example.enterprisedigitalbankingsystem.customer.dto.request.CreateCustomerRequest;
import org.example.enterprisedigitalbankingsystem.customer.dto.request.UpdateCustomerRequest;
import org.example.enterprisedigitalbankingsystem.customer.dto.response.CustomerResponse;
import org.example.enterprisedigitalbankingsystem.customer.dto.response.CustomerSummaryResponse;
import org.example.enterprisedigitalbankingsystem.customer.service.CustomerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping
    public ResponseEntity<CustomerResponse> createCustomer(@Valid @RequestBody CreateCustomerRequest request) {
        CustomerResponse response = customerService.createCustomer(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{customerId}")
    public ResponseEntity<CustomerResponse> getCustomerById(@PathVariable Long customerId) {
        CustomerResponse response = customerService.getCustomerById(customerId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<CustomerResponse> getCustomerByUserId(@PathVariable UUID userId) {
        CustomerResponse response = customerService.getCustomerByUserId(userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<CustomerSummaryResponse>> getAllCustomers() {
        List<CustomerSummaryResponse> response = customerService.getAllCustomers();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{customerId}")
    public ResponseEntity<CustomerResponse> updateCustomer(
            @PathVariable Long customerId,
            @Valid @RequestBody UpdateCustomerRequest request
    ) {
        CustomerResponse response = customerService.updateCustomer(customerId, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{customerId}/kyc")
    public ResponseEntity<CustomerResponse> completeKYC(
            @PathVariable Long customerId,
            @Valid @RequestBody CompleteKYCRequest request
    ) {
        CustomerResponse response = customerService.completeKYC(customerId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{customerId}")
    public ResponseEntity<Void> deleteCustomer(@PathVariable Long customerId) {
        customerService.deleteCustomer(customerId);
        return ResponseEntity.noContent().build();
    }
}
