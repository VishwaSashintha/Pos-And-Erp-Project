package com.gradge.erp.customer.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.gradge.erp.common.entity.BaseEntity;
import com.gradge.erp.tenant.entity.Tenant;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(
        name = "customers",
        indexes = {
                @Index(name = "idx_customer_tenant", columnList = "tenant_id"),
                @Index(name = "idx_customer_phone", columnList = "phone")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Customer extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String phone;

    private String email;

    private String address;

    private String nic;

    @Column(name = "total_spent")
    @Builder.Default
    private Double totalSpent = 0.0;

    @Column(name = "visit_count")
    @Builder.Default
    private Integer visitCount = 0;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "tenant_id",
            nullable = false,
            insertable = false,
            updatable = false
    )
    private Tenant tenant;

    @Column(name = "branch_id")
    private UUID branchId;
}