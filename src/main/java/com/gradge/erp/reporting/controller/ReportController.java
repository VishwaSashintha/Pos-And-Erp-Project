package com.gradge.erp.reporting.controller;

import com.gradge.erp.common.response.ApiResponse;
import com.gradge.erp.reporting.dto.InventoryReportDTO;
import com.gradge.erp.reporting.dto.SalesReportDTO;
import com.gradge.erp.reporting.service.ReportService;
import com.gradge.erp.tenant.context.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('VIEW_REPORTS')")
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/sales")
    public ApiResponse<SalesReportDTO> salesReport() {
        UUID tenantId = TenantContext.getTenantId();
        return ApiResponse.success("Sales report retrieved successfully", reportService.getSalesReport(tenantId));
    }

    @GetMapping("/inventory")
    public ApiResponse<InventoryReportDTO> inventoryReport() {
        UUID tenantId = TenantContext.getTenantId();
        return ApiResponse.success("Inventory report retrieved successfully", reportService.getInventoryReport(tenantId));
    }

    @GetMapping("/export/excel")
    @PreAuthorize("hasAuthority('EXPORT_REPORTS')")
    public ResponseEntity<byte[]> exportExcel() {
        UUID tenantId = TenantContext.getTenantId();
        byte[] excelBytes = reportService.generateExcelReport(tenantId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"light-business-report-" + tenantId + ".xlsx\"")
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excelBytes);
    }

    @GetMapping("/export/csv/sales")
    @PreAuthorize("hasAuthority('EXPORT_REPORTS')")
    public ResponseEntity<byte[]> exportSalesCsv() {
        UUID tenantId = TenantContext.getTenantId();
        byte[] csvBytes = reportService.generateSalesCsv(tenantId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"sales-report-" + tenantId + ".csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csvBytes);
    }

    @GetMapping("/export/csv/inventory")
    @PreAuthorize("hasAuthority('EXPORT_REPORTS')")
    public ResponseEntity<byte[]> exportInventoryCsv() {
        UUID tenantId = TenantContext.getTenantId();
        byte[] csvBytes = reportService.generateInventoryCsv(tenantId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"inventory-report-" + tenantId + ".csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csvBytes);
    }
}
