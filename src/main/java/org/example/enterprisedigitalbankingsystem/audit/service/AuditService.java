package org.example.enterprisedigitalbankingsystem.audit.service;

import org.example.enterprisedigitalbankingsystem.audit.dto.response.AuditLogResponse;
import org.example.enterprisedigitalbankingsystem.audit.entity.AuditAction;
import org.example.enterprisedigitalbankingsystem.auth.entity.User;

import java.util.List;
import java.util.UUID;

public interface AuditService {
    void log(User performedBy, AuditAction action, String entityName, String entityId,
              String oldValue, String newValue, String description, String ipAddress);

    void log(AuditAction action, String entityName, String entityId,
              String oldValue, String newValue, String description);

    List<AuditLogResponse> getAllAuditLogs();
    List<AuditLogResponse> getAuditLogsByEntity(String entityName, String entityId);
    List<AuditLogResponse> getAuditLogsByUser(UUID userId);
}
