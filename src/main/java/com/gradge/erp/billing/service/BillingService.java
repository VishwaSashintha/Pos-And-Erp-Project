package com.gradge.erp.billing.service;

import com.gradge.erp.billing.entity.BillingRecord;
import com.gradge.erp.billing.entity.TenantSubscription;
import com.gradge.erp.billing.gateway.PaymentGateway;
import com.gradge.erp.billing.model.SubscriptionPlanType;
import com.gradge.erp.billing.repository.BillingRepository;
import com.gradge.erp.billing.repository.TenantSubscriptionRepository;
import com.gradge.erp.notification.service.NotificationService;
import com.gradge.erp.tenant.entity.Tenant;
import com.gradge.erp.tenant.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BillingService {

    private final BillingRepository billingRepository;
    private final TenantSubscriptionRepository subscriptionRepository;
    private final TenantRepository tenantRepository;
    private final PaymentGateway paymentGateway;
    private final NotificationService notificationService;

    /** Creates a pending billing record and returns the payment URL. */
    @Transactional
    public Map<String, Object> initiateSubscription(UUID tenantId, SubscriptionPlanType planType) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new RuntimeException("Tenant not found"));

        BigDecimal amount = getPlanPrice(planType);

        Map<String, String> gatewayResult = paymentGateway.initiatePayment(
                tenantId.toString(), amount, "USD", planType.name() + " Subscription"
        );

        BillingRecord record = BillingRecord.builder()
                .tenant(tenant)
                .planType(planType)
                .amount(amount.doubleValue())
                .currency("USD")
                .paymentMethod("GATEWAY")
                .paymentStatus("PENDING")
                .transactionRef(gatewayResult.get("paymentRef"))
                .createdAt(LocalDateTime.now())
                .build();
        billingRepository.save(record);

        return Map.of(
                "billingId", record.getId(),
                "paymentUrl", gatewayResult.get("paymentUrl"),
                "paymentRef", gatewayResult.get("paymentRef")
        );
    }

    @Transactional
    public Map<String, Object> createManualBilling(UUID tenantId, SubscriptionPlanType planType, Double amount) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new RuntimeException("Tenant not found"));

        BillingRecord record = BillingRecord.builder()
                .tenant(tenant)
                .planType(planType)
                .amount(amount)
                .currency("USD")
                .paymentMethod("MANUAL")
                .paymentStatus("PENDING")
                .createdAt(LocalDateTime.now())
                .build();
        billingRepository.save(record);
        return Map.of("billingId", record.getId(), "status", "PENDING");
    }

    @Transactional
    public BillingRecord markAsPaid(UUID billingId, String transactionRef) {
        BillingRecord record = billingRepository.findById(billingId)
                .orElseThrow(() -> new RuntimeException("Billing record not found"));

        String confirmedRef = paymentGateway.confirmPayment(transactionRef);
        record.setPaymentStatus("PAID");
        record.setTransactionRef(confirmedRef);
        record.setPaidAt(LocalDateTime.now());
        BillingRecord saved = billingRepository.save(record);

        activateSubscription(record.getTenant(), record.getPlanType());

        notificationService.sendEmail(
                record.getTenant().getName() + " team",
                "Subscription Activated - Light Business",
                "Your " + record.getPlanType().name() + " subscription is now active."
        );
        log.info("Billing {} marked as paid. Subscription activated for tenant {}",
                billingId, record.getTenant().getId());
        return saved;
    }

    @Transactional
    public TenantSubscription upgradePlan(UUID tenantId, SubscriptionPlanType newPlan) {
        TenantSubscription sub = subscriptionRepository.findByTenantId(tenantId)
                .orElseThrow(() -> new RuntimeException("No active subscription found"));
        log.info("Plan change: tenant={} {} -> {}", tenantId, sub.getPlanType(), newPlan);
        sub.setPlanType(newPlan);
        sub.setEndDate(java.time.LocalDate.now().plusMonths(1));
        return subscriptionRepository.save(sub);
    }

    @Transactional
    public void cancelSubscription(UUID tenantId) {
        TenantSubscription sub = subscriptionRepository.findByTenantId(tenantId)
                .orElseThrow(() -> new RuntimeException("No active subscription found"));
        sub.setActive(false);
        subscriptionRepository.save(sub);
        log.info("Subscription cancelled for tenant {}", tenantId);
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

    private BigDecimal getPlanPrice(SubscriptionPlanType plan) {
        return switch (plan) {
            case FREE -> BigDecimal.ZERO;
            case STARTER -> new BigDecimal("29.00");
            case PRO -> new BigDecimal("79.00");
            case ENTERPRISE -> new BigDecimal("199.00");
        };
    }
}
