package com.gradge.erp.billing.service;

import com.gradge.erp.billing.model.FeatureKey;
import com.gradge.erp.billing.model.SubscriptionPlanType;

import java.util.*;

public class PlanFeatureMatrix {

    private static final Map<SubscriptionPlanType, Set<FeatureKey>> PLAN_FEATURES = new HashMap<>();

    static {

        PLAN_FEATURES.put(SubscriptionPlanType.FREE, Set.of(
                FeatureKey.POS,
                FeatureKey.CRM
        ));

        PLAN_FEATURES.put(SubscriptionPlanType.STARTER, Set.of(
                FeatureKey.POS,
                FeatureKey.CRM,
                FeatureKey.INVENTORY,
                FeatureKey.WORKSHOP
        ));

        PLAN_FEATURES.put(SubscriptionPlanType.PRO, Set.of(
                FeatureKey.POS,
                FeatureKey.CRM,
                FeatureKey.INVENTORY,
                FeatureKey.WORKSHOP,
                FeatureKey.REPORTING,
                FeatureKey.DASHBOARD,
                FeatureKey.NOTIFICATIONS
        ));

        PLAN_FEATURES.put(SubscriptionPlanType.ENTERPRISE, EnumSet.allOf(FeatureKey.class));
    }

    public static boolean hasAccess(SubscriptionPlanType plan, FeatureKey feature) {
        return PLAN_FEATURES.getOrDefault(plan, Set.of()).contains(feature);
    }
}
