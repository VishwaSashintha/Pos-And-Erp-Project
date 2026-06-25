package com.gradge.erp.reporting.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalesReportDTO {

    private Double totalSales;
    private Double totalTax;
    private Double totalDiscount;
    private Double netRevenue;
    private Integer invoiceCount;
}
