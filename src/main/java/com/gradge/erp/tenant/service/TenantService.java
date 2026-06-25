package com.gradge.erp.tenant.service;

import com.gradge.erp.tenant.context.TenantContext;
import org.springframework.stereotype.Service;

import java.util.UUID;

import com.gradge.erp.tenant.repository.TenantRepository;
import com.gradge.erp.tenant.entity.Tenant;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;

@Service
@RequiredArgsConstructor
public class TenantService {

    private final TenantRepository tenantRepository;

    public UUID getCurrentTenantId() {
        UUID tenantId = TenantContext.getTenantId();

        if (tenantId == null) {
            throw new RuntimeException("Tenant not found in request context");
        }

        return tenantId;
    }

    @Cacheable(value = "tenant_api_limits", key = "#tenantId")
    public int getApiLimit(UUID tenantId) {
        return tenantRepository.findById(tenantId)
                .map(Tenant::getApiLimitPerMinute)
                .orElse(100); // default limit
    }
}
