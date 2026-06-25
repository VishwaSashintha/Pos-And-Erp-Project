package com.gradge.erp.workshop.dto;

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
public class JobCardResponseDto {

    private UUID id;
    private String jobNumber;
    private String vehicleNumber;
    private String status;
    private Double laborCost;
    private Double partsCost;
    private Double totalCost;
    private boolean invoiceGenerated;
    private UUID customerId;
    private String customerName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
