package com.gradge.erp.audit.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "audit_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "username")
    private String username;

    @Column(name = "action", nullable = false)
    private String action;

    @Column(name = "entity_name")
    private String entityName;

    @Column(name = "payload", columnDefinition = "TEXT")
    private String payload;

    @Column(name = "timestamp")
    private LocalDateTime timestamp;
}
