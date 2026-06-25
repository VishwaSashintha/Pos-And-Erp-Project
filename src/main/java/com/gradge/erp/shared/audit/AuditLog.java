package com.gradge.erp.shared.audit;

import java.time.Instant;
import java.util.UUID;

public record AuditLog(

        UUID tenantId,

        String action,

        String username,

        String details,

        Instant createdAt

) {
}