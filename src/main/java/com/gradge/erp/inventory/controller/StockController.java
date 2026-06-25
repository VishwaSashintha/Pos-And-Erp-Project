package com.gradge.erp.inventory.controller;

import com.gradge.erp.common.response.ApiResponse;
import com.gradge.erp.inventory.dto.StockAdjustmentRequestDto;
import com.gradge.erp.inventory.dto.StockMovementMapper;
import com.gradge.erp.inventory.dto.StockMovementResponseDto;
import com.gradge.erp.inventory.entity.Product;
import com.gradge.erp.inventory.entity.StockMovement;
import com.gradge.erp.inventory.service.ProductService;
import com.gradge.erp.inventory.service.StockService;
import com.gradge.erp.tenant.entity.Tenant;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/stock")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('MANAGE_PRODUCTS')")
public class StockController {

    private final StockService stockService;
    private final ProductService productService;
    private final StockMovementMapper stockMovementMapper;

    @GetMapping("/{tenantId}/{productId}")
    public ApiResponse<Double> getStock(
            @PathVariable("tenantId") UUID tenantId,
            @PathVariable("productId") UUID productId
    ) {
        Product product = productService.getProduct(productId, tenantId);
        Tenant tenant = new Tenant();
        tenant.setId(tenantId);
        Double stock = stockService.getStock(product, tenant);
        return ApiResponse.success("Stock retrieved successfully", stock);
    }

    @PostMapping("/{tenantId}/adjust")
    public ApiResponse<Void> adjustStock(
            @PathVariable("tenantId") UUID tenantId,
            @Valid @RequestBody StockAdjustmentRequestDto request
    ) {
        Product product = productService.getProduct(request.getProductId(), tenantId);
        Tenant tenant = new Tenant();
        tenant.setId(tenantId);

        int currentProductQty = product.getQuantity() != null ? product.getQuantity() : 0;

        if ("IN".equalsIgnoreCase(request.getType())) {
            stockService.addStock(product, request.getQuantity(), tenant, request.getReference());
            product.setQuantity(currentProductQty + request.getQuantity().intValue());
        } else {
            stockService.reduceStock(product, request.getQuantity(), tenant, request.getReference());
            product.setQuantity(currentProductQty - request.getQuantity().intValue());
        }
        productService.updateProduct(product.getId(), product, tenantId);
        return ApiResponse.success("Stock adjusted successfully", null);
    }

    @GetMapping("/{tenantId}/movements")
    public ApiResponse<List<StockMovementResponseDto>> getMovements(
            @PathVariable("tenantId") UUID tenantId
    ) {
        List<StockMovement> movements = stockService.getMovements(tenantId);
        return ApiResponse.success("Stock movements retrieved successfully", stockMovementMapper.toResponseDtoList(movements));
    }

    @GetMapping("/{tenantId}/movements/{productId}")
    public ApiResponse<List<StockMovementResponseDto>> getMovementsForProduct(
            @PathVariable("tenantId") UUID tenantId,
            @PathVariable("productId") UUID productId
    ) {
        List<StockMovement> movements = stockService.getMovementsForProduct(productId, tenantId);
        return ApiResponse.success("Product stock movements retrieved successfully", stockMovementMapper.toResponseDtoList(movements));
    }
}
