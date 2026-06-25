package com.gradge.erp.shared.tenant;

import java.util.UUID;

public interface TenantAware {

    UUID getTenantId();

}