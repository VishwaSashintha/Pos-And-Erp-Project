package com.gradge.erp.billing;

import com.gradge.erp.billing.guard.FeatureGuard;
import com.gradge.erp.billing.model.AppModule;
import com.gradge.erp.billing.model.FeatureKey;
import com.gradge.erp.tenant.entity.Tenant;
import com.gradge.erp.tenant.repository.TenantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class FeatureGuardIntegrationTest {

    @Autowired
    private FeatureGuard featureGuard;

    @Autowired
    private TenantRepository tenantRepository;

    private Tenant posTenant;
    private Tenant noPosTenant;

    @BeforeEach
    void setUp() {
        posTenant = new Tenant();
        posTenant.setName("POS Enabled Garage");
        posTenant.setEnabledModules(Set.of(AppModule.POS, AppModule.ACCOUNTING));
        posTenant = tenantRepository.save(posTenant);

        noPosTenant = new Tenant();
        noPosTenant.setName("Accounting Only Garage");
        noPosTenant.setEnabledModules(Set.of(AppModule.ACCOUNTING));
        noPosTenant = tenantRepository.save(noPosTenant);
    }

    @Test
    void featureGuard_allowsAccessWhenFeatureEnabled() {
        assertDoesNotThrow(() -> featureGuard.check(posTenant.getId(), FeatureKey.POS));
    }

    @Test
    void featureGuard_blocksAccessWhenFeatureDisabled() {
        assertThatThrownBy(() -> featureGuard.check(noPosTenant.getId(), FeatureKey.POS))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Feature not available in your plan: POS");
    }
}
