package com.gradge.erp.security.validation;

import org.springframework.stereotype.Component;

@Component
public class RequestValidationUtil {

    public void validateTenantId(String tenantId) {

        if (tenantId == null || tenantId.isBlank()) {
            throw new RuntimeException("Missing tenantId header");
        }

        if (tenantId.length() < 10) {
            throw new RuntimeException("Invalid tenantId");
        }
    }
}
