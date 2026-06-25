package com.gradge.erp.reporting.controller;

import com.gradge.erp.common.response.ApiResponse;
import com.gradge.erp.reporting.dto.InventoryReportDTO;
import com.gradge.erp.reporting.dto.SalesReportDTO;
import com.gradge.erp.reporting.service.ReportService;
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
    public ApiResponse<SalesReportDTO> salesReport(@RequestHeader("tenantId") UUID tenantId) {
        return ApiResponse.success("Sales report retrieved successfully", reportService.getSalesReport(tenantId));
    }

    @GetMapping("/inventory")
    public ApiResponse<InventoryReportDTO> inventoryReport(@RequestHeader("tenantId") UUID tenantId) {
        return ApiResponse.success("Inventory report retrieved successfully", reportService.getInventoryReport(tenantId));
    }

    /**
     * Downloads an Excel (.xlsx) workbook containing Sales and Inventory sheets.
     * GET /api/reports/export/excel
     * Header: tenantId: <uuid>
     */
    @GetMapping("/export/excel")
    public ResponseEntity<byte[]> exportExcel(@RequestHeader("tenantId") UUID tenantId) {
        byte[] excelBytes = reportService.generateExcelReport(tenantId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"gradge-report-" + tenantId + ".xlsx\"")
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excelBytes);
    }
}
