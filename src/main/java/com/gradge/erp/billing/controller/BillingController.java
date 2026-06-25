package com.gradge.erp.billing.controller;

import com.gradge.erp.billing.entity.BillingRecord;
import com.gradge.erp.billing.entity.TenantSubscription;
import com.gradge.erp.billing.model.SubscriptionPlanType;
import com.gradge.erp.billing.service.BillingService;
import com.gradge.erp.common.response.ApiResponse;
import com.gradge.erp.tenant.context.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/billing")
@RequiredArgsConstructor
public class BillingController {

    private final BillingService billingService;

    /** Initiate a subscription payment via the payment gateway */
    @PostMapping("/subscribe")
    @PreAuthorize("hasAuthority('MANAGE_BILLING')")
    public ApiResponse<Map<String, Object>> subscribe(@RequestParam SubscriptionPlanType planType) {
        UUID tenantId = TenantContext.getTenantId();
        Map<String, Object> result = billingService.initiateSubscription(tenantId, planType);
        return ApiResponse.success("Payment initiated", result);
    }

    /** Create a manual billing record (admin/cash payments) */
    @PostMapping("/manual")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ApiResponse<Map<String, Object>> manual(
            @RequestParam UUID tenantId,
            @RequestParam SubscriptionPlanType planType,
            @RequestParam Double amount) {
        return ApiResponse.success("Manual billing created", billingService.createManualBilling(tenantId, planType, amount));
    }

    /** Mark a billing record as paid (webhook callback or admin confirmation) */
    @PostMapping("/pay/{id}")
    @PreAuthorize("hasAuthority('MANAGE_BILLING')")
    public ApiResponse<BillingRecord> pay(@PathVariable("id") UUID id, @RequestParam String transactionRef) {
        BillingRecord record = billingService.markAsPaid(id, transactionRef);
        return ApiResponse.success("Billing payment processed", record);
    }

    /** Upgrade or downgrade a subscription plan */
    @PutMapping("/plan")
    @PreAuthorize("hasAuthority('MANAGE_BILLING')")
    public ApiResponse<TenantSubscription> changePlan(@RequestParam SubscriptionPlanType newPlan) {
        UUID tenantId = TenantContext.getTenantId();
        return ApiResponse.success("Plan updated", billingService.upgradePlan(tenantId, newPlan));
    }

    /** Cancel subscription */
    @DeleteMapping("/cancel")
    @PreAuthorize("hasAuthority('MANAGE_BILLING')")
    public ApiResponse<String> cancel() {
        UUID tenantId = TenantContext.getTenantId();
        billingService.cancelSubscription(tenantId);
        return ApiResponse.success("Subscription cancelled");
    }
}
