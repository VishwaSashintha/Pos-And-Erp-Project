package com.gradge.erp.shared.tenant;

import java.util.UUID;

public interface TenantProvider {

    UUID getCurrentTenantId();

}