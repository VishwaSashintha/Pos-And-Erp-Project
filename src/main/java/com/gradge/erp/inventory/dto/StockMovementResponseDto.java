package com.gradge.erp.inventory.dto;

import com.gradge.erp.inventory.enums.StockMovementType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StockMovementResponseDto {
    private UUID id;
    private UUID productId;
    private String productName;
    private StockMovementType type;
    private Double quantity;
    private String reference;
    private UUID tenantId;
    private LocalDateTime createdAt;
}
