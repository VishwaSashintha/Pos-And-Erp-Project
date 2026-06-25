package com.gradge.erp.shared.audit;

public interface AuditService {

    void log(
            AuditAction action,
            String entityType,
            String entityId,
            String description
    );
}
