package org.example.enterprisedigitalbankingsystem.audit.repository;

import org.example.enterprisedigitalbankingsystem.audit.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AuditRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findByEntityNameAndEntityIdOrderByCreatedAtDesc(String entityName, String entityId);
    List<AuditLog> findByPerformedBy_UserIdOrderByCreatedAtDesc(UUID userId);
    List<AuditLog> findAllByOrderByCreatedAtDesc();
}
