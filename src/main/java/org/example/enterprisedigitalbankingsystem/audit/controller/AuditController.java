package org.example.enterprisedigitalbankingsystem.audit.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.example.enterprisedigitalbankingsystem.audit.dto.request.AuditSearchRequest;
import org.example.enterprisedigitalbankingsystem.common.dto.PagedResponse;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.ModelAttribute;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.example.enterprisedigitalbankingsystem.audit.dto.response.AuditLogResponse;
import org.example.enterprisedigitalbankingsystem.audit.service.AuditService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Validated
@Tag(name = "Audit Logs", description = "Immutable banking audit trail queries and dynamic search APIs")
@RestController
@RequestMapping("/api/v1/audit")
@RequiredArgsConstructor
public class AuditController {

    private final AuditService auditService;

    @GetMapping
    @Operation(summary = "Get paginated list of audit logs")
    public ResponseEntity<PagedResponse<AuditLogResponse>> getAllAuditLogs(
            @ParameterObject @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(auditService.getAllAuditLogs(pageable));
    }

    @GetMapping("/entity/{entityName}/{entityId}")
    @Operation(summary = "Get paginated audit logs for a specific entity")
    public ResponseEntity<PagedResponse<AuditLogResponse>> getAuditLogsByEntity(
            @PathVariable @NotBlank(message = "Entity name is required")
            @Size(max = 80, message = "Entity name cannot exceed 80 characters") String entityName,
            @PathVariable @NotBlank(message = "Entity id is required")
            @Size(max = 80, message = "Entity id cannot exceed 80 characters") String entityId,
            @ParameterObject @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(auditService.getAuditLogsByEntity(entityName, entityId, pageable));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get paginated audit logs performed by a specific user")
    public ResponseEntity<PagedResponse<AuditLogResponse>> getAuditLogsByUser(
            @PathVariable @NotNull(message = "User id is required") UUID userId,
            @ParameterObject @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(auditService.getAuditLogsByUser(userId, pageable));
    }

    @GetMapping("/search")
    @Operation(summary = "Search audit logs dynamically by user, action, entity, date range, and keyword")
    public ResponseEntity<PagedResponse<AuditLogResponse>> searchAuditLogs(
            @ParameterObject @Valid @ModelAttribute AuditSearchRequest filter,
            @ParameterObject @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(auditService.searchAuditLogs(filter, pageable));
    }
}
