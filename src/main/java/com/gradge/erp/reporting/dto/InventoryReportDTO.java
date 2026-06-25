package com.gradge.erp.reporting.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryReportDTO {

    private Integer totalProducts;
    private Double totalStockValue;
    private Integer lowStockItems;
}
