package org.example.enterprisedigitalbankingsystem.common.util;

import org.example.enterprisedigitalbankingsystem.account.entity.Account;
import org.example.enterprisedigitalbankingsystem.common.dto.PagedResponse;
import org.example.enterprisedigitalbankingsystem.exception.BadRequestException;
import org.example.enterprisedigitalbankingsystem.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.core.PropertyReferenceException;
import org.springframework.data.core.TypeInformation;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PaginationAndSortingTest {

    private static final Set<String> ACCOUNT_SORT_FIELDS = Set.of("id", "accountNumber", "balance", "createdAt");
    private static final Sort DEFAULT_SORT = Sort.by(Sort.Direction.DESC, "createdAt");

    @Test
    @DisplayName("PagedResponse.from preserves pagination metadata and Java Serializable compatibility")
    void pagedResponse_fromAndSerialize_preservesMetadata() throws Exception {
        PageImpl<String> page = new PageImpl<>(List.of("A", "B"), PageRequest.of(1, 2), 5);
        PagedResponse<String> response = PagedResponse.from(page);

        assertEquals(List.of("A", "B"), response.getContent());
        assertEquals(1, response.getPage());
        assertEquals(2, response.getSize());
        assertEquals(5L, response.getTotalElements());
        assertEquals(3, response.getTotalPages());
        assertFalse(response.isFirst());
        assertFalse(response.isLast());
        assertFalse(response.isEmpty());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(response);
        }
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()))) {
            @SuppressWarnings("unchecked")
            PagedResponse<String> deserialized = (PagedResponse<String>) ois.readObject();
            assertEquals(List.of("A", "B"), deserialized.getContent());
            assertEquals(5L, deserialized.getTotalElements());
        }
    }

    @Test
    @DisplayName("SortValidator applies default sort + id tie-breaker and blocks disallowed fields")
    void sortValidator_validatesAndAppendsTieBreaker() {
        Pageable unpagedValidated = SortValidator.validate(Pageable.unpaged(), ACCOUNT_SORT_FIELDS, DEFAULT_SORT);
        assertEquals(0, unpagedValidated.getPageNumber());
        assertEquals(20, unpagedValidated.getPageSize());
        assertEquals(Sort.Direction.DESC, unpagedValidated.getSort().getOrderFor("createdAt").getDirection());
        assertEquals(Sort.Direction.DESC, unpagedValidated.getSort().getOrderFor("id").getDirection());

        Pageable balanceAsc = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "balance"));
        Pageable validatedAsc = SortValidator.validate(balanceAsc, ACCOUNT_SORT_FIELDS, DEFAULT_SORT);
        assertEquals(Sort.Direction.ASC, validatedAsc.getSort().getOrderFor("balance").getDirection());
        Sort.Order tieOrder = validatedAsc.getSort().getOrderFor("id");
        assertNotNull(tieOrder);
        assertEquals(Sort.Direction.ASC, tieOrder.getDirection());

        Pageable alreadySortedById = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "id"));
        Pageable idValidated = SortValidator.validate(alreadySortedById, ACCOUNT_SORT_FIELDS, DEFAULT_SORT);
        assertEquals(1L, idValidated.getSort().stream().count());

        Pageable disallowed = PageRequest.of(0, 10, Sort.by("passwordHash"));
        BadRequestException ex = assertThrows(
                BadRequestException.class,
                () -> SortValidator.validate(disallowed, ACCOUNT_SORT_FIELDS, DEFAULT_SORT)
        );
        assertTrue(ex.getMessage().contains("passwordHash"));
    }

    @Test
    @DisplayName("GlobalExceptionHandler maps PropertyReferenceException to 400 and OptimisticLockingFailureException to 409")
    void globalExceptionHandler_mapsSortingAndOptimisticLockExceptions() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new ExceptionThrowingController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        mockMvc.perform(get("/test/property-ref"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Invalid sort property 'nonExistentField'"));

        mockMvc.perform(get("/test/optimistic-lock"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Resource was modified concurrently. Please retry your request."));
    }

    @RestController
    static class ExceptionThrowingController {

        @GetMapping("/test/property-ref")
        String throwPropertyRef() {
            throw new PropertyReferenceException("nonExistentField", TypeInformation.of(Account.class), List.of());
        }

        @GetMapping("/test/optimistic-lock")
        String throwOptimisticLock() {
            throw new ObjectOptimisticLockingFailureException(Account.class, 1L);
        }
    }
}

