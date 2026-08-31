package org.example.enterprisedigitalbankingsystem.audit.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.enterprisedigitalbankingsystem.auth.entity.User;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "performed_by")
    private User performedBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuditAction action;

    @Column(name = "entity_name", nullable = false)
    private String entityName;

    @Column(name = "entity_id")
    private String entityId;

    @Lob
    @Column(name = "old_value")
    private String oldValue;

    @Lob
    @Column(name = "new_value")
    private String newValue;

    @Column(length = 255)
    private String description;

    @Column(name = "ip_address", length = 64)
    private String ipAddress;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
    }
}
