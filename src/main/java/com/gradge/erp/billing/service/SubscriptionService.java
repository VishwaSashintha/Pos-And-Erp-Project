package com.gradge.erp.billing.service;

import com.gradge.erp.billing.model.FeatureKey;
import com.gradge.erp.billing.model.AppModule;
import com.gradge.erp.tenant.entity.Tenant;
import com.gradge.erp.tenant.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

import org.springframework.cache.annotation.Cacheable;

@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final TenantRepository tenantRepository;

    @Cacheable(value = "featureAccess", key = "(#tenantId != null ? #tenantId.toString() : 'null') + '-' + #feature.name()")
    public boolean hasFeatureAccess(UUID tenantId, FeatureKey feature) {
        Tenant tenant = tenantRepository.findById(tenantId).orElse(null);
        if (tenant == null) {
            return false;
        }

        // If legacy tenant with no modules, default to true for basic compatibility
        if (tenant.getEnabledModules() == null || tenant.getEnabledModules().isEmpty()) {
            return true;
        }

        AppModule requiredModule = mapFeatureKeyToAppModule(feature);
        return tenant.getEnabledModules().contains(requiredModule);
    }

    private AppModule mapFeatureKeyToAppModule(FeatureKey key) {
        if (key == null) return AppModule.ACCOUNTING;
        switch (key) {
            case POS:
                return AppModule.POS;
            case INVENTORY:
                return AppModule.INVENTORY;
            case WORKSHOP:
                return AppModule.INVENTORY;
            case CRM:
                return AppModule.CRM;
            case REPORTING:
            case DASHBOARD:
                return AppModule.ACCOUNTING;
            default:
                return AppModule.ACCOUNTING;
        }
    }
}
