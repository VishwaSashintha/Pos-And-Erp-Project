package com.gradge.erp.workshop.controller;

import com.gradge.erp.common.response.ApiResponse;
import com.gradge.erp.pos.entity.Invoice;
import com.gradge.erp.tenant.entity.Tenant;
import com.gradge.erp.workshop.dto.JobCardMapper;
import com.gradge.erp.workshop.dto.JobCardRequestDto;
import com.gradge.erp.workshop.dto.JobCardResponseDto;
import com.gradge.erp.workshop.entity.JobCard;
import com.gradge.erp.workshop.service.JobCardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/jobcards")
@RequiredArgsConstructor
public class JobCardController {

    private final JobCardService jobCardService;
    private final JobCardMapper jobCardMapper;

    @PostMapping
    public ApiResponse<JobCardResponseDto> create(@Valid @RequestBody JobCardRequestDto dto) {
        JobCard entity = jobCardMapper.toEntity(dto);
        JobCard saved = jobCardService.createJobCard(entity);
        return ApiResponse.success("Job card created successfully", jobCardMapper.toResponseDto(saved));
    }

    @GetMapping("/{tenantId}")
    public ApiResponse<List<JobCardResponseDto>> getAll(@PathVariable("tenantId") UUID tenantId) {
        List<JobCard> jobCards = jobCardService.getAllJobCards(tenantId);
        return ApiResponse.success(jobCardMapper.toResponseDtoList(jobCards));
    }

    @GetMapping("/{tenantId}/{id}")
    public ApiResponse<JobCardResponseDto> get(
            @PathVariable("tenantId") UUID tenantId,
            @PathVariable("id") UUID id
    ) {
        JobCard jobCard = jobCardService.getJobCard(id, tenantId);
        return ApiResponse.success(jobCardMapper.toResponseDto(jobCard));
    }

    @PutMapping("/{tenantId}/{id}")
    public ApiResponse<JobCardResponseDto> update(
            @PathVariable("tenantId") UUID tenantId,
            @PathVariable("id") UUID id,
            @Valid @RequestBody JobCardRequestDto dto
    ) {
        JobCard entity = jobCardMapper.toEntity(dto);
        JobCard updated = jobCardService.updateJobCard(id, entity, tenantId);
        return ApiResponse.success("Job card updated successfully", jobCardMapper.toResponseDto(updated));
    }

    @DeleteMapping("/{tenantId}/{id}")
    public ApiResponse<Void> delete(
            @PathVariable("tenantId") UUID tenantId,
            @PathVariable("id") UUID id
    ) {
        jobCardService.deleteJobCard(id, tenantId);
        return ApiResponse.success("Job card deleted successfully", null);
    }

    @PostMapping("/{jobCardId}/generate-invoice")
    public ApiResponse<Invoice> generateInvoice(
            @PathVariable("jobCardId") UUID jobCardId,
            @RequestHeader("tenantId") UUID tenantId
    ) {
        Tenant tenant = new Tenant();
        tenant.setId(tenantId);
        Invoice invoice = jobCardService.generateInvoiceFromJobCard(jobCardId, tenant);
        return ApiResponse.success("Invoice generated from job card", invoice);
    }
}
