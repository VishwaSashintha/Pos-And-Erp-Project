package com.gradge.erp.purchase.controller;

import com.gradge.erp.common.response.ApiResponse;
import com.gradge.erp.purchase.dto.PurchaseOrderMapper;
import com.gradge.erp.purchase.dto.PurchaseOrderResponseDto;
import com.gradge.erp.purchase.entity.GoodsReceivedNote;
import com.gradge.erp.purchase.entity.PurchaseOrder;
import com.gradge.erp.purchase.service.PurchaseService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/purchases")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('MANAGE_PURCHASES')")
public class PurchaseController {

    private final PurchaseService purchaseService;
    private final PurchaseOrderMapper purchaseOrderMapper;

    @PostMapping
    public ApiResponse<PurchaseOrderResponseDto> createOrder(@RequestBody PurchaseOrder order) {
        PurchaseOrder saved = purchaseService.createPurchaseOrder(order);
        return ApiResponse.success("Purchase order created successfully", purchaseOrderMapper.toResponseDto(saved));
    }

    @GetMapping("/{tenantId}")
    public ApiResponse<List<PurchaseOrderResponseDto>> getAll(@PathVariable("tenantId") UUID tenantId) {
        List<PurchaseOrder> orders = purchaseService.getAllOrders(tenantId);
        return ApiResponse.success(purchaseOrderMapper.toResponseDtoList(orders));
    }

    @GetMapping("/{tenantId}/{id}")
    public ApiResponse<PurchaseOrderResponseDto> get(
            @PathVariable("tenantId") UUID tenantId,
            @PathVariable("id") UUID id
    ) {
        PurchaseOrder order = purchaseService.getOrder(id, tenantId);
        return ApiResponse.success(purchaseOrderMapper.toResponseDto(order));
    }

    @PutMapping("/{tenantId}/{id}/submit")
    public ApiResponse<PurchaseOrderResponseDto> submit(
            @PathVariable("tenantId") UUID tenantId,
            @PathVariable("id") UUID id
    ) {
        PurchaseOrder submitted = purchaseService.submitOrder(id, tenantId);
        return ApiResponse.success("Purchase order submitted", purchaseOrderMapper.toResponseDto(submitted));
    }

    @PostMapping("/{tenantId}/{id}/receive")
    public ApiResponse<GoodsReceivedNote> receiveGoods(
            @PathVariable("tenantId") UUID tenantId,
            @PathVariable("id") UUID purchaseOrderId,
            @Valid @RequestBody ReceiveGoodsRequest request
    ) {
        GoodsReceivedNote grn = purchaseService.receiveGoods(
                purchaseOrderId,
                request.getProductId(),
                request.getQuantity(),
                request.getNotes(),
                tenantId
        );
        return ApiResponse.success("Goods received successfully", grn);
    }

    @GetMapping("/{tenantId}/grns")
    public ApiResponse<List<GoodsReceivedNote>> getAllGRNs(@PathVariable("tenantId") UUID tenantId) {
        return ApiResponse.success(purchaseService.getAllGRNs(tenantId));
    }

    @GetMapping("/grns/{purchaseOrderId}")
    public ApiResponse<List<GoodsReceivedNote>> getGRNsForOrder(@PathVariable("purchaseOrderId") UUID purchaseOrderId) {
        return ApiResponse.success(purchaseService.getGRNsForOrder(purchaseOrderId));
    }

    @Data
    public static class ReceiveGoodsRequest {
        @NotNull(message = "Product ID is required")
        private UUID productId;
        @Positive(message = "Quantity must be positive")
        private Double quantity;
        private String notes;
    }
}
