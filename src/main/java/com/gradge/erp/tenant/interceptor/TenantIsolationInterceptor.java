package com.gradge.erp.tenant.interceptor;

import com.gradge.erp.tenant.context.TenantContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

import java.util.Map;
import java.util.UUID;

@Component
public class TenantIsolationInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        @SuppressWarnings("unchecked")
        Map<String, String> pathVariables = (Map<String, String>) request.getAttribute(
                HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE
        );

        if (pathVariables != null && pathVariables.containsKey("tenantId")) {
            String pathTenantIdStr = pathVariables.get("tenantId");
            if (pathTenantIdStr != null) {
                try {
                    UUID pathTenantId = UUID.fromString(pathTenantIdStr);
                    UUID authenticatedTenantId = TenantContext.getTenantId();

                    if (authenticatedTenantId == null || !authenticatedTenantId.equals(pathTenantId)) {
                        response.sendError(HttpServletResponse.SC_FORBIDDEN, "Tenant context mismatch");
                        return false;
                    }
                } catch (IllegalArgumentException e) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid tenant ID format");
                    return false;
                }
            }
        }
        return true;
    }
}
