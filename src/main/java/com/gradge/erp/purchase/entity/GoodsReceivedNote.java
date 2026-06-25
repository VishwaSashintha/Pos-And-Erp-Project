package com.gradge.erp.purchase.entity;

import com.gradge.erp.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(
        name = "goods_received_notes",
        indexes = {
                @Index(name = "idx_grn_tenant", columnList = "tenant_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GoodsReceivedNote extends BaseEntity {

    @Column(name = "grn_number", nullable = false, unique = true)
    private String grnNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_order_id", nullable = false)
    private PurchaseOrder purchaseOrder;

    @Column(name = "received_date")
    private LocalDate receivedDate;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "quantity_received", nullable = false)
    private Double quantityReceived;

    private String notes;

    @Builder.Default
    private boolean stockUpdated = false;
}
