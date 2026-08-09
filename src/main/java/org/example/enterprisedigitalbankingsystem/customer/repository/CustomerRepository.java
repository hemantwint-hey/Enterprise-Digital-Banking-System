package org.example.enterprisedigitalbankingsystem.customer.repository;

import org.example.enterprisedigitalbankingsystem.customer.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByUserUserId(UUID userId);
    Optional<Customer> findByPanNumber(String panNumber);
    Optional<Customer> findByAadhaarNumber(String aadhaarNumber);
    Optional<Customer> findByPhoneNumber(String phoneNumber);

    boolean existsByPhoneNumber(String phoneNumber);
    boolean existsByPanNumber(String panNumber);
    boolean existsByAadhaarNumber(String aadhaarNumber);
}
