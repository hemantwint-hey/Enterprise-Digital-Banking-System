package org.example.enterprisedigitalbankingsystem.audit.mapper;

import org.example.enterprisedigitalbankingsystem.audit.dto.response.AuditLogResponse;
import org.example.enterprisedigitalbankingsystem.audit.entity.AuditLog;
import org.example.enterprisedigitalbankingsystem.auth.entity.User;
import org.springframework.stereotype.Component;

@Component
public class AuditMapper {

    public AuditLogResponse toResponse(AuditLog auditLog) {
        if (auditLog == null) {
            return null;
        }

        User performedBy = auditLog.getPerformedBy();

        return AuditLogResponse.builder()
                .id(auditLog.getId())
                .performedByUserId(performedBy != null ? performedBy.getUserId() : null)
                .performedByUsername(performedBy != null ? performedBy.getUsername() : null)
                .action(auditLog.getAction())
                .entityName(auditLog.getEntityName())
                .entityId(auditLog.getEntityId())
                .oldValue(auditLog.getOldValue())
                .newValue(auditLog.getNewValue())
                .description(auditLog.getDescription())
                .ipAddress(auditLog.getIpAddress())
                .createdAt(auditLog.getCreatedAt())
                .build();
    }
}
