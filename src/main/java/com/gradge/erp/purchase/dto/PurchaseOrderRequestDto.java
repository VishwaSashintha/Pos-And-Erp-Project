package com.gradge.erp.purchase.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PurchaseOrderRequestDto {

    @NotNull(message = "Supplier ID is required")
    private UUID supplierId;

    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    private String notes;

    private BigDecimal totalAmount;

    private List<PurchaseOrderItemDto> items;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PurchaseOrderItemDto {
        @NotNull(message = "Product ID is required")
        private UUID productId;
        private Double quantity;
        private BigDecimal unitCost;
        private BigDecimal totalCost;
    }
}
