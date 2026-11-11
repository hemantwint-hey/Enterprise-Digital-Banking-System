package org.example.enterprisedigitalbankingsystem.audit.specification;

import jakarta.persistence.criteria.Predicate;
import org.example.enterprisedigitalbankingsystem.audit.dto.request.AuditSearchRequest;
import org.example.enterprisedigitalbankingsystem.audit.entity.AuditLog;
import org.example.enterprisedigitalbankingsystem.common.util.SpecificationHelper;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/**
 * Dynamic Spring Data JPA Specifications for multi-criteria audit log search.
 */
public final class AuditSpecification {

    private AuditSpecification() {
    }

    public static Specification<AuditLog> withFilters(AuditSearchRequest filter) {
        return (root, query, cb) -> {
            if (filter == null) {
                return cb.conjunction();
            }

            List<Predicate> predicates = new ArrayList<>();

            if (filter.getPerformedByUserId() != null) {
                predicates.add(cb.equal(root.get("performedBy").get("userId"), filter.getPerformedByUserId()));
            }
            if (filter.getAction() != null) {
                predicates.add(cb.equal(root.get("action"), filter.getAction()));
            }
            if (SpecificationHelper.hasText(filter.getEntityName())) {
                predicates.add(cb.equal(root.get("entityName"), filter.getEntityName().trim()));
            }
            if (SpecificationHelper.hasText(filter.getEntityId())) {
                predicates.add(cb.equal(root.get("entityId"), filter.getEntityId().trim()));
            }
            if (filter.getFromDate() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), filter.getFromDate()));
            }
            if (filter.getToDate() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), filter.getToDate()));
            }
            if (SpecificationHelper.hasText(filter.getDescriptionKeyword())) {
                predicates.add(SpecificationHelper.containsIgnoreCase(
                        cb, root.get("description"), filter.getDescriptionKeyword()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}

