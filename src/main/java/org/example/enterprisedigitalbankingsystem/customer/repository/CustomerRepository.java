package org.example.enterprisedigitalbankingsystem.customer.repository;

import org.example.enterprisedigitalbankingsystem.auth.entity.User;
import org.example.enterprisedigitalbankingsystem.customer.entity.Customer;
import org.hibernate.internal.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByUser(User user);
    Optional<Customer> findByUserId(Long userId);
    Optional<Customer> findByPanNumber(String panNo);
    Optional<Customer> findByAadharNumber(String aadharNumber);
    Optional<Customer> findByPhoneNumber(String phoneNumber);

    boolean existByPhoneNumber(String phoneNumber);
    boolean existByPanNumber(String panNumber);
    boolean existByAadharNumber(String aadharNumber);
}
