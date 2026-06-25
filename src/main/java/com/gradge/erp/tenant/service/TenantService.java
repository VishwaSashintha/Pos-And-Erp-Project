package com.gradge.erp.tenant.service;

import com.gradge.erp.tenant.context.TenantContext;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class TenantService {

    public UUID getCurrentTenantId() {
        UUID tenantId = TenantContext.getTenantId();

        if (tenantId == null) {
            throw new RuntimeException("Tenant not found in request context");
        }

        return tenantId;
    }
}
