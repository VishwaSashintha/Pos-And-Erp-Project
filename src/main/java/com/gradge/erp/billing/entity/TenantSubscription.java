package com.gradge.erp.billing.entity;

import com.gradge.erp.billing.model.SubscriptionPlanType;
import com.gradge.erp.tenant.entity.Tenant;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "tenant_subscriptions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TenantSubscription {

    @Id
    @GeneratedValue
    private UUID id;

    @OneToOne
    private Tenant tenant;

    @Enumerated(EnumType.STRING)
    private SubscriptionPlanType planType;

    private LocalDate startDate;

    private LocalDate endDate;

    private boolean active;
}
