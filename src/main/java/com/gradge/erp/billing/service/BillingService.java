package com.gradge.erp.billing.service;

import com.gradge.erp.billing.entity.BillingRecord;
import com.gradge.erp.billing.entity.TenantSubscription;
import com.gradge.erp.billing.model.SubscriptionPlanType;
import com.gradge.erp.billing.repository.BillingRepository;
import com.gradge.erp.billing.repository.TenantSubscriptionRepository;
import com.gradge.erp.tenant.entity.Tenant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BillingService {

    private final BillingRepository billingRepository;
    private final TenantSubscriptionRepository subscriptionRepository;

    
    
    
    public BillingRecord createBilling(Tenant tenant, SubscriptionPlanType planType, Double amount) {

        BillingRecord record = BillingRecord.builder()
                .tenant(tenant)
                .planType(planType)
                .amount(amount)
                .currency("USD")
                .paymentMethod("PENDING")
                .paymentStatus("PENDING")
                .createdAt(LocalDateTime.now())
                .build();

        return billingRepository.save(record);
    }

    
    
    
    public BillingRecord markAsPaid(UUID billingId, String transactionRef) {

        BillingRecord record = billingRepository.findById(billingId)
                .orElseThrow(() -> new RuntimeException("Billing record not found"));

        record.setPaymentStatus("PAID");
        record.setTransactionRef(transactionRef);
        record.setPaidAt(LocalDateTime.now());

        BillingRecord saved = billingRepository.save(record);

        
        activateSubscription(record.getTenant(), record.getPlanType());

        return saved;
    }

    
    
    
    private void activateSubscription(Tenant tenant, SubscriptionPlanType planType) {

        TenantSubscription subscription = subscriptionRepository.findByTenantId(tenant.getId()).orElse(null);

        if (subscription == null) {
            subscription = new TenantSubscription();
            subscription.setTenant(tenant);
        }

        subscription.setPlanType(planType);
        subscription.setActive(true);
        subscription.setStartDate(java.time.LocalDate.now());
        subscription.setEndDate(java.time.LocalDate.now().plusMonths(1));

        subscriptionRepository.save(subscription);
    }
}
