package org.example.enterprisedigitalbankingsystem.audit.repository;

import org.example.enterprisedigitalbankingsystem.audit.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AuditRepository extends JpaRepository<AuditLog, Long>, JpaSpecificationExecutor<AuditLog> {

    @EntityGraph(attributePaths = {"performedBy"})
    @Override
    Page<AuditLog> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"performedBy"})
    @Override
    Page<AuditLog> findAll(Specification<AuditLog> spec, Pageable pageable);

    @EntityGraph(attributePaths = {"performedBy"})
    Page<AuditLog> findByEntityNameAndEntityId(String entityName, String entityId, Pageable pageable);

    @EntityGraph(attributePaths = {"performedBy"})
    Page<AuditLog> findByPerformedBy_UserId(UUID userId, Pageable pageable);
}
