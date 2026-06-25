package com.gradge.erp.workshop.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InspectionItemResponseDto {
    private UUID id;
    private UUID inspectionId;
    private String itemName;
    private boolean checked;
    private String remarks;
}
