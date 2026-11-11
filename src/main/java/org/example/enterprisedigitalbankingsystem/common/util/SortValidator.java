package org.example.enterprisedigitalbankingsystem.common.util;

import org.example.enterprisedigitalbankingsystem.exception.BadRequestException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Set;

/**
 * Enforces per-entity sort field allowlists, applies default sorting when absent,
 * and appends a deterministic primary-key tie-breaker ("id") for stable pagination.
 */
public final class SortValidator {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 20;

    private SortValidator() {
    }

    public static Pageable validate(Pageable pageable, Set<String> allowedFields, Sort defaultSort) {
        if (pageable == null || pageable.isUnpaged()) {
            return PageRequest.of(DEFAULT_PAGE, DEFAULT_SIZE, ensureDeterministicIdSort(defaultSort));
        }

        Sort requestedSort = pageable.getSort();
        if (requestedSort.isUnsorted()) {
            return PageRequest.of(
                    pageable.getPageNumber(),
                    pageable.getPageSize(),
                    ensureDeterministicIdSort(defaultSort)
            );
        }

        for (Sort.Order order : requestedSort) {
            String property = order.getProperty();
            if (!allowedFields.contains(property)) {
                throw new BadRequestException(
                        "Invalid sort field '" + property + "'. Allowed fields: " + allowedFields
                );
            }
        }

        return PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                ensureDeterministicIdSort(requestedSort)
        );
    }

    private static Sort ensureDeterministicIdSort(Sort sort) {
        if (sort == null || sort.isUnsorted()) {
            return Sort.by(Sort.Direction.DESC, "id");
        }
        if (sort.getOrderFor("id") != null) {
            return sort;
        }
        Sort.Direction tieDirection = sort.stream()
                .findFirst()
                .map(Sort.Order::getDirection)
                .orElse(Sort.Direction.DESC);
        return sort.and(Sort.by(tieDirection, "id"));
    }
}

