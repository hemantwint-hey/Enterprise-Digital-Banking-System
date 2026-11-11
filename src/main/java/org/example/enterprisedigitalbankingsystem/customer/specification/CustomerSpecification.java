package org.example.enterprisedigitalbankingsystem.customer.specification;

import jakarta.persistence.criteria.Predicate;
import org.example.enterprisedigitalbankingsystem.common.util.SpecificationHelper;
import org.example.enterprisedigitalbankingsystem.customer.dto.request.CustomerSearchRequest;
import org.example.enterprisedigitalbankingsystem.customer.entity.Customer;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/**
 * Dynamic Spring Data JPA Specifications for multi-criteria customer search.
 */
public final class CustomerSpecification {

    private CustomerSpecification() {
    }

    public static Specification<Customer> withFilters(CustomerSearchRequest filter) {
        return (root, query, cb) -> {
            if (filter == null) {
                return cb.conjunction();
            }

            List<Predicate> predicates = new ArrayList<>();

            if (filter.getCustomerId() != null) {
                predicates.add(cb.equal(root.get("id"), filter.getCustomerId()));
            }
            if (SpecificationHelper.hasText(filter.getFirstName())) {
                predicates.add(SpecificationHelper.containsIgnoreCase(cb, root.get("firstName"), filter.getFirstName()));
            }
            if (SpecificationHelper.hasText(filter.getLastName())) {
                predicates.add(SpecificationHelper.containsIgnoreCase(cb, root.get("lastName"), filter.getLastName()));
            }
            if (SpecificationHelper.hasText(filter.getPhoneNumber())) {
                predicates.add(cb.equal(root.get("phoneNumber"), filter.getPhoneNumber().trim()));
            }
            if (SpecificationHelper.hasText(filter.getCity())) {
                predicates.add(SpecificationHelper.containsIgnoreCase(cb, root.get("city"), filter.getCity()));
            }
            if (SpecificationHelper.hasText(filter.getState())) {
                predicates.add(SpecificationHelper.containsIgnoreCase(cb, root.get("state"), filter.getState()));
            }
            if (filter.getKycStatus() != null) {
                predicates.add(cb.equal(root.get("kycStatus"), filter.getKycStatus()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}

