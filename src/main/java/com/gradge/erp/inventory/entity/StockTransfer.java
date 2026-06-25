package com.gradge.erp.inventory.entity;

import com.gradge.erp.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "stock_transfers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockTransfer extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_warehouse_id", nullable = false)
    private Warehouse sourceWarehouse;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destination_warehouse_id", nullable = false)
    private Warehouse destinationWarehouse;

    @Column(name = "reference_number", unique = true, nullable = false)
    private String referenceNumber;

    @Column(name = "transfer_date", nullable = false)
    private LocalDateTime transferDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private com.gradge.erp.inventory.enums.TransferStatus status;

    @Column(length = 500)
    private String notes;

    @OneToMany(mappedBy = "stockTransfer", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<StockTransferItem> items = new ArrayList<>();

    public void addItem(StockTransferItem item) {
        items.add(item);
        item.setStockTransfer(this);
    }
}
