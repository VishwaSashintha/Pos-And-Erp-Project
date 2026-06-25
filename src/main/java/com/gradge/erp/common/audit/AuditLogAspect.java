package com.gradge.erp.common.audit;

import com.gradge.erp.audit.entity.AuditLog;
import com.gradge.erp.audit.repository.AuditLogRepository;
import com.gradge.erp.tenant.context.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class AuditLogAspect {

    private final AuditLogRepository auditLogRepository;

    @Around("@annotation(auditable)")
    public Object logAuditActivity(ProceedingJoinPoint joinPoint, Auditable auditable) throws Throwable {
        String username = "SYSTEM";
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !authentication.getPrincipal().equals("anonymousUser")) {
            username = authentication.getName();
        }

        AuditLog auditLog = AuditLog.builder()
                .tenantId(TenantContext.getTenantId())
                .username(username)
                .action(auditable.action())
                .entityName(joinPoint.getSignature().getDeclaringType().getSimpleName())
                .payload(joinPoint.getSignature().getName() + " executed")
                .timestamp(LocalDateTime.now())
                .build();

        try {
            Object result = joinPoint.proceed();
            auditLogRepository.save(auditLog);
            return result;
        } catch (Throwable e) {
            auditLog.setPayload(e.getMessage());
            auditLogRepository.save(auditLog);
            throw e;
        }
    }
}
