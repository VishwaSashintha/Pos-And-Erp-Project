package com.gradge.erp.workshop.dto;

import com.gradge.erp.workshop.enums.InspectionStatus;
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
public class InspectionResponseDto {
    private UUID id;
    private UUID jobCardId;
    private InspectionStatus status;
    private String overallNotes;
    private Double estimatedCost;
    private UUID tenantId;
    private UUID branchId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
