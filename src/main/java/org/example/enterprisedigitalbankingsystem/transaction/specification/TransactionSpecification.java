package org.example.enterprisedigitalbankingsystem.transaction.specification;

import jakarta.persistence.criteria.Predicate;
import org.example.enterprisedigitalbankingsystem.common.util.SpecificationHelper;
import org.example.enterprisedigitalbankingsystem.transaction.dto.request.TransactionSearchRequest;
import org.example.enterprisedigitalbankingsystem.transaction.entity.Transaction;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/**
 * Dynamic Spring Data JPA Specifications for multi-criteria transaction filtering.
 */
public final class TransactionSpecification {

    private TransactionSpecification() {
    }

    public static Specification<Transaction> withFilters(TransactionSearchRequest filter) {
        return (root, query, cb) -> {
            if (filter == null) {
                return cb.conjunction();
            }

            List<Predicate> predicates = new ArrayList<>();

            if (filter.getAccountId() != null) {
                Predicate isSource = cb.equal(root.get("sourceAccount").get("id"), filter.getAccountId());
                Predicate isDest = cb.equal(root.get("destinationAccount").get("id"), filter.getAccountId());
                predicates.add(cb.or(isSource, isDest));
            }
            if (SpecificationHelper.hasText(filter.getTransactionReference())) {
                predicates.add(cb.equal(root.get("transactionReference"), filter.getTransactionReference().trim()));
            }
            if (filter.getTransactionType() != null) {
                predicates.add(cb.equal(root.get("transactionType"), filter.getTransactionType()));
            }
            if (filter.getTransactionStatus() != null) {
                predicates.add(cb.equal(root.get("transactionStatus"), filter.getTransactionStatus()));
            }
            if (filter.getMinAmount() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("amount"), filter.getMinAmount()));
            }
            if (filter.getMaxAmount() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("amount"), filter.getMaxAmount()));
            }
            if (filter.getFromDate() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), filter.getFromDate()));
            }
            if (filter.getToDate() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), filter.getToDate()));
            }
            if (SpecificationHelper.hasText(filter.getRemarksKeyword())) {
                predicates.add(SpecificationHelper.containsIgnoreCase(
                        cb, root.get("remarks"), filter.getRemarksKeyword()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}

