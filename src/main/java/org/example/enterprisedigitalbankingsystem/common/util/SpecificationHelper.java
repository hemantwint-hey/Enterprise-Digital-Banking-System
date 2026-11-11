package org.example.enterprisedigitalbankingsystem.common.util;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;

/**
 * Shared JPA Criteria predicates with SQL LIKE wildcard escaping.
 */
public final class SpecificationHelper {

    private static final char ESCAPE_CHAR = '\\';

    private SpecificationHelper() {
    }

    /**
     * Escapes LIKE wildcard characters ('\\', '%', '_') before applying a case-insensitive substring filter.
     */
    public static Predicate containsIgnoreCase(
            CriteriaBuilder cb,
            Expression<String> field,
            String rawInput) {
        String escaped = rawInput.trim().toLowerCase()
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
        return cb.like(cb.lower(field), "%" + escaped + "%", ESCAPE_CHAR);
    }

    public static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}

