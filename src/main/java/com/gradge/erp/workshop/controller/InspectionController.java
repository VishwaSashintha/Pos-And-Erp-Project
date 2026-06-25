package com.gradge.erp.workshop.controller;

import com.gradge.erp.common.response.ApiResponse;
import com.gradge.erp.workshop.dto.InspectionItemRequestDto;
import com.gradge.erp.workshop.dto.InspectionItemResponseDto;
import com.gradge.erp.workshop.dto.InspectionMapper;
import com.gradge.erp.workshop.dto.InspectionResponseDto;
import com.gradge.erp.workshop.entity.Inspection;
import com.gradge.erp.workshop.entity.InspectionItem;
import com.gradge.erp.workshop.service.InspectionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/inspections")
@RequiredArgsConstructor
public class InspectionController {

    private final InspectionService inspectionService;
    private final InspectionMapper inspectionMapper;

    @GetMapping("/job/{jobCardId}/{tenantId}")
    public ApiResponse<List<InspectionResponseDto>> getByJobCard(
            @PathVariable("jobCardId") UUID jobCardId,
            @PathVariable("tenantId") UUID tenantId
    ) {
        List<Inspection> inspections = inspectionService.getByJobCard(jobCardId, tenantId);
        return ApiResponse.success("Inspections retrieved successfully", inspectionMapper.toResponseDtoList(inspections));
    }

    @PutMapping("/{tenantId}/{id}/start")
    public ApiResponse<InspectionResponseDto> start(
            @PathVariable("tenantId") UUID tenantId,
            @PathVariable("id") UUID id
    ) {
        Inspection inspection = inspectionService.startInspection(id, tenantId);
        return ApiResponse.success("Inspection started successfully", inspectionMapper.toResponseDto(inspection));
    }

    @PutMapping("/{tenantId}/{id}/complete")
    public ApiResponse<InspectionResponseDto> complete(
            @PathVariable("tenantId") UUID tenantId,
            @PathVariable("id") UUID id,
            @RequestParam String notes
    ) {
        Inspection inspection = inspectionService.completeInspection(id, tenantId, notes);
        return ApiResponse.success("Inspection completed successfully", inspectionMapper.toResponseDto(inspection));
    }

    @PutMapping("/{tenantId}/{id}/approve")
    public ApiResponse<InspectionResponseDto> approve(
            @PathVariable("tenantId") UUID tenantId,
            @PathVariable("id") UUID id
    ) {
        Inspection inspection = inspectionService.approveInspection(id, tenantId);
        return ApiResponse.success("Inspection approved successfully", inspectionMapper.toResponseDto(inspection));
    }

    @PostMapping("/{inspectionId}/item")
    public ApiResponse<InspectionItemResponseDto> addItem(
            @PathVariable("inspectionId") UUID inspectionId,
            @Valid @RequestBody InspectionItemRequestDto itemDto
    ) {
        InspectionItem item = inspectionMapper.toEntity(itemDto);
        InspectionItem savedItem = inspectionService.addItem(inspectionId, item);
        return ApiResponse.success("Inspection item added successfully", inspectionMapper.toResponseDto(savedItem));
    }

    @GetMapping("/{inspectionId}/items")
    public ApiResponse<List<InspectionItemResponseDto>> getItems(@PathVariable("inspectionId") UUID inspectionId) {
        List<InspectionItem> items = inspectionService.getItems(inspectionId);
        return ApiResponse.success("Inspection items retrieved successfully", inspectionMapper.toItemResponseDtoList(items));
    }
}
