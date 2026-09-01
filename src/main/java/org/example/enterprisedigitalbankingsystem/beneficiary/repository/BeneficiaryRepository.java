package org.example.enterprisedigitalbankingsystem.beneficiary.repository;

import org.example.enterprisedigitalbankingsystem.beneficiary.entity.Beneficiary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BeneficiaryRepository extends JpaRepository<Beneficiary, Long> {
    List<Beneficiary> findByCustomerId(Long customerId);
    boolean existsByCustomerIdAndBeneficiaryAccountId(Long customerId, Long accountId);
}
