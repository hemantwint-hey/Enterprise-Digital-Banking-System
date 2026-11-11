package org.example.enterprisedigitalbankingsystem.audit.service;

import org.example.enterprisedigitalbankingsystem.audit.dto.request.AuditSearchRequest;
import org.example.enterprisedigitalbankingsystem.audit.dto.response.AuditLogResponse;
import org.example.enterprisedigitalbankingsystem.audit.entity.AuditAction;
import org.example.enterprisedigitalbankingsystem.auth.entity.User;
import org.example.enterprisedigitalbankingsystem.common.dto.PagedResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface AuditService {
    void log(User performedBy, AuditAction action, String entityName, String entityId,
             String oldValue, String newValue, String description, String ipAddress);

    void log(AuditAction action, String entityName, String entityId,
             String oldValue, String newValue, String description);

    PagedResponse<AuditLogResponse> getAllAuditLogs(Pageable pageable);
    PagedResponse<AuditLogResponse> getAuditLogsByEntity(String entityName, String entityId, Pageable pageable);
    PagedResponse<AuditLogResponse> getAuditLogsByUser(UUID userId, Pageable pageable);
    PagedResponse<AuditLogResponse> searchAuditLogs(AuditSearchRequest filter, Pageable pageable);
}
