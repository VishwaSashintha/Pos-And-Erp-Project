package com.gradge.erp.billing.guard;

import com.gradge.erp.billing.model.FeatureKey;
import com.gradge.erp.billing.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class FeatureGuard {

    private final SubscriptionService subscriptionService;

    public void check(UUID tenantId, FeatureKey feature) {

        boolean allowed = subscriptionService.hasFeatureAccess(tenantId, feature);

        if (!allowed) {
            throw new RuntimeException("Feature not available in your plan: " + feature);
        }
    }
}
