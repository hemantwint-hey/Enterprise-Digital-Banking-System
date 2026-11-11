package org.example.enterprisedigitalbankingsystem.audit.service.impl;

import org.example.enterprisedigitalbankingsystem.audit.dto.request.AuditSearchRequest;
import org.example.enterprisedigitalbankingsystem.audit.specification.AuditSpecification;
import org.example.enterprisedigitalbankingsystem.common.dto.PagedResponse;
import org.example.enterprisedigitalbankingsystem.common.util.SortValidator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import java.util.Set;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.example.enterprisedigitalbankingsystem.audit.dto.response.AuditLogResponse;
import org.example.enterprisedigitalbankingsystem.audit.entity.AuditAction;
import org.example.enterprisedigitalbankingsystem.audit.entity.AuditLog;
import org.example.enterprisedigitalbankingsystem.audit.mapper.AuditMapper;
import org.example.enterprisedigitalbankingsystem.audit.repository.AuditRepository;
import org.example.enterprisedigitalbankingsystem.audit.service.AuditService;
import org.example.enterprisedigitalbankingsystem.auth.entity.User;
import org.example.enterprisedigitalbankingsystem.security.CustomUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

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
                .ipAddress(ipAddress != null ? ipAddress : resolveClientIp())
                .build();

        auditRepository.save(auditLog);
    }

    @Override
    public void log(AuditAction action, String entityName, String entityId,
                     String oldValue, String newValue, String description) {
        log(resolveCurrentUser(), action, entityName, entityId, oldValue, newValue, description, null);
    }

    private User resolveCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
            return userDetails.getUser();
        }
        return null;
    }

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "id", "createdAt", "action", "entityName"
    );
    private static final Sort DEFAULT_SORT = Sort.by(Sort.Direction.DESC, "createdAt");

    /**
     * Resolves client IP using servlet remoteAddr. Behind reverse proxies in prod,
     * server.forward-headers-strategy=native lets Tomcat rewrite remoteAddr from
     * trusted proxy headers without blindly trusting arbitrary X-Forwarded-For values.
     */
    String resolveClientIp() {
        if (!(RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes)) {
            return null;
        }
        return attributes.getRequest().getRemoteAddr();
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<AuditLogResponse> getAllAuditLogs(Pageable pageable) {
        Pageable validatedPageable = SortValidator.validate(pageable, ALLOWED_SORT_FIELDS, DEFAULT_SORT);
        Page<AuditLogResponse> page = auditRepository.findAll(validatedPageable)
                .map(auditMapper::toResponse);
        return PagedResponse.from(page);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<AuditLogResponse> getAuditLogsByEntity(
            String entityName, String entityId, Pageable pageable) {
        Pageable validatedPageable = SortValidator.validate(pageable, ALLOWED_SORT_FIELDS, DEFAULT_SORT);
        Page<AuditLogResponse> page = auditRepository
                .findByEntityNameAndEntityId(entityName, entityId, validatedPageable)
                .map(auditMapper::toResponse);
        return PagedResponse.from(page);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<AuditLogResponse> getAuditLogsByUser(UUID userId, Pageable pageable) {
        Pageable validatedPageable = SortValidator.validate(pageable, ALLOWED_SORT_FIELDS, DEFAULT_SORT);
        Page<AuditLogResponse> page = auditRepository
                .findByPerformedBy_UserId(userId, validatedPageable)
                .map(auditMapper::toResponse);
        return PagedResponse.from(page);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<AuditLogResponse> searchAuditLogs(AuditSearchRequest filter, Pageable pageable) {
        Pageable validatedPageable = SortValidator.validate(pageable, ALLOWED_SORT_FIELDS, DEFAULT_SORT);
        Page<AuditLogResponse> page = auditRepository
                .findAll(AuditSpecification.withFilters(filter), validatedPageable)
                .map(auditMapper::toResponse);
        return PagedResponse.from(page);
    }
}
