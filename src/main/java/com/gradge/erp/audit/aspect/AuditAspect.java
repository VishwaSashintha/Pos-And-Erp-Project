package com.gradge.erp.audit.aspect;

import com.gradge.erp.audit.annotation.Auditable;
import com.gradge.erp.audit.service.AuditLogService;
import com.gradge.erp.tenant.context.TenantContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.UUID;

@Aspect
@Component
@RequiredArgsConstructor
public class AuditAspect {

    private final AuditLogService auditLogService;
    private final ObjectMapper objectMapper;

    @AfterReturning(pointcut = "@annotation(auditable)", returning = "result")
    public void auditAction(JoinPoint joinPoint, Auditable auditable, Object result) {
        try {
            String username = "system";
            if (SecurityContextHolder.getContext().getAuthentication() != null) {
                username = SecurityContextHolder.getContext().getAuthentication().getName();
            }

            UUID tenantId = TenantContext.getTenantId();
            if (tenantId == null) {
                tenantId = UUID.fromString("00000000-0000-0000-0000-000000000000");
            }

            String entityId = "";
            String entityName = "";
            String payload = "";

            if (result != null) {
                entityName = result.getClass().getSimpleName();
                try {
                    Method getIdMethod = result.getClass().getMethod("getId");
                    Object idVal = getIdMethod.invoke(result);
                    if (idVal != null) {
                        entityId = idVal.toString();
                    }
                } catch (Exception e) {
                }
                payload = objectMapper.writeValueAsString(result);
            } else {
                Object[] args = joinPoint.getArgs();
                if (args.length > 0) {
                    payload = objectMapper.writeValueAsString(args[0]);
                }
            }

            auditLogService.log(
                    auditable.action(),
                    entityName,
                    entityId,
                    username,
                    tenantId,
                    payload
            );
        } catch (Exception e) {
            System.err.println("Audit logging failed: " + e.getMessage());
        }
    }
}
