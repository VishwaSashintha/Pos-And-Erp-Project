package com.gradge.erp.audit.service;

import com.gradge.erp.audit.entity.AuditLog;
import com.gradge.erp.audit.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public void log(String action, String entityName, String entityId, String username, UUID tenantId, String payload) {
        AuditLog log = AuditLog.builder()
                .action(action)
                .entityName(entityName)
                .entityId(entityId)
                .username(username)
                .tenantId(tenantId)
                .payload(payload)
                .timestamp(LocalDateTime.now())
                .build();
        auditLogRepository.save(log);
    }

    public List<AuditLog> getLogsByTenant(UUID tenantId) {
        return auditLogRepository.findByTenantIdOrderByTimestampDesc(tenantId);
    }
}
