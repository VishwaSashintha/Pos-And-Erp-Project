package com.gradge.erp.dashboard.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardDTO {

    
    private Double totalRevenue;
    private Double todayRevenue;
    private Double monthlyRevenue;

    
    private Integer totalInvoices;
    private Integer pendingInvoices;
    private Integer paidInvoices;

    
    private Integer activeJobCards;
    private Integer completedJobs;

    
    private Integer totalProducts;
    private Integer lowStockItems;

    
    private Integer totalCustomers;
}
