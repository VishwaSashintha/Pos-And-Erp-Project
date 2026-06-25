package com.gradge.erp.inventory.controller;

import com.gradge.erp.common.response.ApiResponse;
import com.gradge.erp.inventory.dto.ProductMapper;
import com.gradge.erp.inventory.dto.ProductRequestDto;
import com.gradge.erp.inventory.dto.ProductResponseDto;
import com.gradge.erp.inventory.entity.Product;
import com.gradge.erp.inventory.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('MANAGE_PRODUCTS')")
public class ProductController {

    private final ProductService productService;
    private final ProductMapper productMapper;

    @PostMapping
    public ApiResponse<ProductResponseDto> create(@Valid @RequestBody ProductRequestDto dto) {
        Product entity = productMapper.toEntity(dto);
        Product saved = productService.createProduct(entity);
        return ApiResponse.success("Product created successfully", productMapper.toResponseDto(saved));
    }

    @GetMapping("/{tenantId}")
    public ApiResponse<List<ProductResponseDto>> getAll(@PathVariable("tenantId") UUID tenantId) {
        List<Product> products = productService.getAllProducts(tenantId);
        return ApiResponse.success(productMapper.toResponseDtoList(products));
    }

    @GetMapping("/{tenantId}/{id}")
    public ApiResponse<ProductResponseDto> get(
            @PathVariable("tenantId") UUID tenantId,
            @PathVariable("id") UUID id
    ) {
        Product product = productService.getProduct(id, tenantId);
        return ApiResponse.success(productMapper.toResponseDto(product));
    }

    @PutMapping("/{tenantId}/{id}")
    public ApiResponse<ProductResponseDto> update(
            @PathVariable("tenantId") UUID tenantId,
            @PathVariable("id") UUID id,
            @Valid @RequestBody ProductRequestDto dto
    ) {
        Product entity = productMapper.toEntity(dto);
        Product updated = productService.updateProduct(id, entity, tenantId);
        return ApiResponse.success("Product updated successfully", productMapper.toResponseDto(updated));
    }

    @DeleteMapping("/{tenantId}/{id}")
    public ApiResponse<Void> delete(
            @PathVariable("tenantId") UUID tenantId,
            @PathVariable("id") UUID id
    ) {
        productService.deleteProduct(id, tenantId);
        return ApiResponse.success("Product deleted successfully", null);
    }

    @GetMapping("/{tenantId}/low-stock")
    public ApiResponse<List<ProductResponseDto>> getLowStock(@PathVariable("tenantId") UUID tenantId) {
        List<Product> products = productService.getLowStockProducts(tenantId);
        return ApiResponse.success(productMapper.toResponseDtoList(products));
    }
}