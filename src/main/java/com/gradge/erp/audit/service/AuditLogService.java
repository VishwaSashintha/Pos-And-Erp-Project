package com.gradge.erp.audit.service;

import com.gradge.erp.audit.entity.AuditLog;
import com.gradge.erp.audit.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public List<AuditLog> getTenantAuditLogs(UUID tenantId) {
        return auditLogRepository.findByTenantIdOrderByTimestampDesc(tenantId);
    }
}
