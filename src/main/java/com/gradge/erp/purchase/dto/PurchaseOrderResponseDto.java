package com.gradge.erp.purchase.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PurchaseOrderResponseDto {

    private UUID id;
    private String poNumber;
    private UUID supplierId;
    private String supplierName;
    private String status;
    private BigDecimal totalAmount;
    private String notes;
    private List<PurchaseOrderItemResponseDto> items;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PurchaseOrderItemResponseDto {
        private UUID id;
        private UUID productId;
        private String productName;
        private Double quantity;
        private BigDecimal unitCost;
        private BigDecimal totalCost;
    }
}
