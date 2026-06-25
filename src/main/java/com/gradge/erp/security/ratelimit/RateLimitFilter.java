package com.gradge.erp.security.ratelimit;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class RateLimitFilter implements Filter {

    private final RateLimiterService rateLimiterService;

    private final com.gradge.erp.tenant.service.TenantService tenantService;

    @Override
    public void doFilter(ServletRequest request,
                         ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        String key = req.getRemoteAddr();
        int limit = 100;

        String tenantIdStr = req.getHeader("X-Tenant-ID");
        if (tenantIdStr != null && !tenantIdStr.isBlank()) {
            try {
                java.util.UUID tenantId = java.util.UUID.fromString(tenantIdStr);
                limit = tenantService.getApiLimit(tenantId);
            } catch (Exception e) {
                // Ignore invalid UUID, fallback to default limit
            }
        }

        if (!rateLimiterService.isAllowed(key, limit)) {
            res.setStatus(429);
            res.getWriter().write("Too many requests - rate limit exceeded");
            return;
        }

        chain.doFilter(request, response);
    }
}
