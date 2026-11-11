package org.example.enterprisedigitalbankingsystem.account.specification;

import jakarta.persistence.criteria.Predicate;
import org.example.enterprisedigitalbankingsystem.account.dto.request.AccountSearchRequest;
import org.example.enterprisedigitalbankingsystem.account.entity.Account;
import org.example.enterprisedigitalbankingsystem.common.util.SpecificationHelper;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/**
 * Dynamic Spring Data JPA Specifications for multi-criteria account search.
 */
public final class AccountSpecification {

    private AccountSpecification() {
    }

    public static Specification<Account> withFilters(AccountSearchRequest filter) {
        return (root, query, cb) -> {
            if (filter == null) {
                return cb.conjunction();
            }

            List<Predicate> predicates = new ArrayList<>();

            if (filter.getCustomerId() != null) {
                predicates.add(cb.equal(root.get("customer").get("id"), filter.getCustomerId()));
            }
            if (SpecificationHelper.hasText(filter.getAccountNumber())) {
                predicates.add(cb.equal(root.get("accountNumber"), filter.getAccountNumber().trim()));
            }
            if (filter.getAccountType() != null) {
                predicates.add(cb.equal(root.get("accountType"), filter.getAccountType()));
            }
            if (filter.getAccountStatus() != null) {
                predicates.add(cb.equal(root.get("accountStatus"), filter.getAccountStatus()));
            }
            if (SpecificationHelper.hasText(filter.getBranch())) {
                predicates.add(SpecificationHelper.containsIgnoreCase(cb, root.get("branch"), filter.getBranch()));
            }
            if (filter.getMinBalance() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("balance"), filter.getMinBalance()));
            }
            if (filter.getMaxBalance() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("balance"), filter.getMaxBalance()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}

