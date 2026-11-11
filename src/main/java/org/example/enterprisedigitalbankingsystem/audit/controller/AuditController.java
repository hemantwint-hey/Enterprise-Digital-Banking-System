package org.example.enterprisedigitalbankingsystem.audit.controller;

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
@RestController
@RequestMapping("/api/v1/audit")
@RequiredArgsConstructor
public class AuditController {

    private final AuditService auditService;

    @GetMapping
    public ResponseEntity<List<AuditLogResponse>> getAllAuditLogs() {
        return ResponseEntity.ok(auditService.getAllAuditLogs());
    }

    @GetMapping("/entity/{entityName}/{entityId}")
    public ResponseEntity<List<AuditLogResponse>> getAuditLogsByEntity(
            @PathVariable @NotBlank(message = "Entity name is required")
            @Size(max = 80, message = "Entity name cannot exceed 80 characters") String entityName,
            @PathVariable @NotBlank(message = "Entity id is required")
            @Size(max = 80, message = "Entity id cannot exceed 80 characters") String entityId) {
        return ResponseEntity.ok(auditService.getAuditLogsByEntity(entityName, entityId));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<AuditLogResponse>> getAuditLogsByUser(
            @PathVariable @NotNull(message = "User id is required") UUID userId) {
        return ResponseEntity.ok(auditService.getAuditLogsByUser(userId));
    }
}
