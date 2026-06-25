package com.gradge.erp.shared.domain;

import java.util.UUID;

public abstract class TenantAwareEntity {

    private UUID tenantId;

    public UUID getTenantId() {
        return tenantId;
    }

    public void setTenantId(UUID tenantId) {
        this.tenantId = tenantId;
    }
}
