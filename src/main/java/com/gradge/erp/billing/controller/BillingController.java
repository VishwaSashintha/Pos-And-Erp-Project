package com.gradge.erp.billing.controller;

import com.gradge.erp.billing.entity.BillingRecord;
import com.gradge.erp.billing.model.SubscriptionPlanType;
import com.gradge.erp.billing.service.BillingService;
import com.gradge.erp.common.response.ApiResponse;
import com.gradge.erp.tenant.entity.Tenant;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/billing")
@RequiredArgsConstructor
public class BillingController {

    private final BillingService billingService;

    @PostMapping("/create")
    public ApiResponse<BillingRecord> createBilling(
            @RequestHeader("tenantId") UUID tenantId,
            @RequestParam SubscriptionPlanType planType,
            @RequestParam Double amount
    ) {
        Tenant tenant = new Tenant();
        tenant.setId(tenantId);
        BillingRecord record = billingService.createBilling(tenant, planType, amount);
        return ApiResponse.success("Billing record created successfully", record);
    }

    @PostMapping("/pay/{id}")
    public ApiResponse<BillingRecord> pay(
            @PathVariable("id") UUID id,
            @RequestParam String transactionRef
    ) {
        BillingRecord record = billingService.markAsPaid(id, transactionRef);
        return ApiResponse.success("Billing payment processed successfully", record);
    }
}
