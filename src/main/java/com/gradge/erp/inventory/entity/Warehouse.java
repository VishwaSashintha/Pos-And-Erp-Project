package com.gradge.erp.inventory.entity;

import com.gradge.erp.common.entity.BaseEntity;
import com.gradge.erp.tenant.entity.Tenant;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "warehouses",
        indexes = {
                @Index(name = "idx_warehouse_tenant", columnList = "tenant_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Warehouse extends BaseEntity {

    @Column(nullable = false)
    private String name;

    private String location;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "tenant_id",
            nullable = false,
            insertable = false,
            updatable = false
    )
    private Tenant tenant;
}
