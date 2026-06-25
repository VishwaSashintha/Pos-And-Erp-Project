package com.gradge.erp.inventory.entity;

import com.gradge.erp.common.entity.BaseEntity;
import com.gradge.erp.tenant.entity.Tenant;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "products",
        indexes = {
                @Index(name = "idx_product_tenant", columnList = "tenant_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product extends BaseEntity {

    @Column(nullable = false)
    private String name;

    private String sku;

    private String barcode;

    @Column(name = "selling_price")
    private Double sellingPrice;

    @Column(name = "cost_price")
    private Double costPrice;

    @Builder.Default
    private Integer quantity = 0;

    @Column(name = "reorder_level")
    private Integer reorderLevel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "tenant_id",
            nullable = false,
            insertable = false,
            updatable = false
    )
    private Tenant tenant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;
}