package org.example.enterprisedigitalbankingsystem.audit.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.enterprisedigitalbankingsystem.audit.dto.response.AuditLogResponse;
import org.example.enterprisedigitalbankingsystem.audit.entity.AuditAction;
import org.example.enterprisedigitalbankingsystem.audit.entity.AuditLog;
import org.example.enterprisedigitalbankingsystem.audit.mapper.AuditMapper;
import org.example.enterprisedigitalbankingsystem.audit.repository.AuditRepository;
import org.example.enterprisedigitalbankingsystem.audit.service.AuditService;
import org.example.enterprisedigitalbankingsystem.auth.entity.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class AuditServiceImpl implements AuditService {

    private final AuditRepository auditRepository;
    private final AuditMapper auditMapper;

    @Override
    public void log(User performedBy, AuditAction action, String entityName, String entityId,
                     String oldValue, String newValue, String description, String ipAddress) {

        AuditLog auditLog = AuditLog.builder()
                .performedBy(performedBy)
                .action(action)
                .entityName(entityName)
                .entityId(entityId)
                .oldValue(oldValue)
                .newValue(newValue)
                .description(description)
                .ipAddress(ipAddress)
                .build();

        auditRepository.save(auditLog);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLogResponse> getAllAuditLogs() {
        return auditRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(auditMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLogResponse> getAuditLogsByEntity(String entityName, String entityId) {
        return auditRepository.findByEntityNameAndEntityIdOrderByCreatedAtDesc(entityName, entityId).stream()
                .map(auditMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLogResponse> getAuditLogsByUser(UUID userId) {
        return auditRepository.findByPerformedBy_UserIdOrderByCreatedAtDesc(userId).stream()
                .map(auditMapper::toResponse)
                .toList();
    }
}
