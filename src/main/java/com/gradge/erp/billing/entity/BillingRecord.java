package com.gradge.erp.billing.entity;

import com.gradge.erp.billing.model.SubscriptionPlanType;
import com.gradge.erp.tenant.entity.Tenant;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "billing_records")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BillingRecord {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne
    private Tenant tenant;

    @Enumerated(EnumType.STRING)
    private SubscriptionPlanType planType;

    private Double amount;

    private String currency;

    private String paymentMethod; 

    private String paymentStatus; 

    private String transactionRef;

    private LocalDateTime createdAt;

    private LocalDateTime paidAt;
}
