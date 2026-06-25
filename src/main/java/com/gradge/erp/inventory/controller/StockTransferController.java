package com.gradge.erp.inventory.controller;

import com.gradge.erp.common.response.ApiResponse;
import com.gradge.erp.inventory.entity.StockTransfer;
import com.gradge.erp.inventory.service.StockTransferService;
import com.gradge.erp.tenant.context.TenantContext;
import com.gradge.erp.tenant.entity.Tenant;
import com.gradge.erp.tenant.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/inventory/transfers")
@RequiredArgsConstructor
public class StockTransferController {

    private final StockTransferService stockTransferService;
    private final TenantRepository tenantRepository;

    private Tenant getCurrentTenant() {
        UUID tenantId = TenantContext.getTenantId();
        return tenantRepository.findById(tenantId)
                .orElseThrow(() -> new RuntimeException("Tenant not found"));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('MANAGE_INVENTORY')")
    public ApiResponse<StockTransfer> createTransfer(
            @RequestParam UUID sourceId,
            @RequestParam UUID destinationId,
            @RequestParam String reference,
            @RequestParam(required = false) String notes) {
        
        StockTransfer transfer = stockTransferService.createTransfer(sourceId, destinationId, reference, notes, getCurrentTenant());
        return ApiResponse.success("Stock transfer drafted", transfer);
    }

    @PostMapping("/{transferId}/execute")
    @PreAuthorize("hasAuthority('MANAGE_INVENTORY')")
    public ApiResponse<String> executeTransfer(@PathVariable UUID transferId) {
        stockTransferService.executeTransfer(transferId, getCurrentTenant());
        return ApiResponse.success("Stock transfer executed successfully");
    }
}
