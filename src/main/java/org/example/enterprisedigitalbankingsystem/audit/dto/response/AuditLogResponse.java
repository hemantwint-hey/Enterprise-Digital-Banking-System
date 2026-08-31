package org.example.enterprisedigitalbankingsystem.audit.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.enterprisedigitalbankingsystem.audit.entity.AuditAction;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AuditLogResponse {
    private Long id;
    private UUID performedByUserId;
    private String performedByUsername;
    private AuditAction action;
    private String entityName;
    private String entityId;
    private String oldValue;
    private String newValue;
    private String description;
    private String ipAddress;
    private LocalDateTime createdAt;
}
