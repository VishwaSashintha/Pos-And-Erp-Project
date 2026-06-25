package com.gradge.erp.common.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Standardized event payload published to RabbitMQ when a POS invoice is confirmed.
 * All downstream services (Inventory, Accounting, Analytics) consume this.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PosSaleCreatedEvent {

    private UUID tenantId;
    private UUID invoiceId;
    private String invoiceNumber;
    private UUID customerId;
    private BigDecimal total;
    private LocalDate saleDate;
    private List<LineItem> items;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LineItem {
        private String productName;
        private int quantity;
        private BigDecimal unitPrice;
        private BigDecimal lineTotal;
    }
}
