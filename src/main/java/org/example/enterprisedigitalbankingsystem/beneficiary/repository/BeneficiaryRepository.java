package org.example.enterprisedigitalbankingsystem.beneficiary.repository;

import org.example.enterprisedigitalbankingsystem.beneficiary.entity.Beneficiary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BeneficiaryRepository extends JpaRepository<Beneficiary, Long> {

    @EntityGraph(attributePaths = {"beneficiaryAccount"})
    Page<Beneficiary> findByCustomerId(Long customerId, Pageable pageable);

    boolean existsByCustomerIdAndBeneficiaryAccountId(Long customerId, Long accountId);
}
