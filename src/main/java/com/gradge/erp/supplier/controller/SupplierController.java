package com.gradge.erp.supplier.controller;

import com.gradge.erp.common.response.ApiResponse;
import com.gradge.erp.supplier.dto.SupplierMapper;
import com.gradge.erp.supplier.dto.SupplierRequestDto;
import com.gradge.erp.supplier.dto.SupplierResponseDto;
import com.gradge.erp.supplier.entity.Supplier;
import com.gradge.erp.supplier.service.SupplierService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/suppliers")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('MANAGE_SUPPLIERS')")
public class SupplierController {

    private final SupplierService supplierService;
    private final SupplierMapper supplierMapper;

    @PostMapping
    public ApiResponse<SupplierResponseDto> create(@Valid @RequestBody SupplierRequestDto dto) {
        Supplier entity = supplierMapper.toEntity(dto);
        Supplier saved = supplierService.createSupplier(entity);
        return ApiResponse.success("Supplier created successfully", supplierMapper.toResponseDto(saved));
    }

    @GetMapping("/{tenantId}")
    public ApiResponse<List<SupplierResponseDto>> getAll(
            @PathVariable("tenantId") UUID tenantId,
            @RequestParam(required = false) String search
    ) {
        List<Supplier> suppliers;
        if (search != null && !search.isBlank()) {
            suppliers = supplierService.searchSuppliers(tenantId, search);
        } else {
            suppliers = supplierService.getAllSuppliers(tenantId);
        }
        return ApiResponse.success(supplierMapper.toResponseDtoList(suppliers));
    }

    @GetMapping("/{tenantId}/{id}")
    public ApiResponse<SupplierResponseDto> get(
            @PathVariable("tenantId") UUID tenantId,
            @PathVariable("id") UUID id
    ) {
        Supplier supplier = supplierService.getSupplier(id, tenantId);
        return ApiResponse.success(supplierMapper.toResponseDto(supplier));
    }

    @PutMapping("/{tenantId}/{id}")
    public ApiResponse<SupplierResponseDto> update(
            @PathVariable("tenantId") UUID tenantId,
            @PathVariable("id") UUID id,
            @Valid @RequestBody SupplierRequestDto dto
    ) {
        Supplier entity = supplierMapper.toEntity(dto);
        Supplier updated = supplierService.updateSupplier(id, entity, tenantId);
        return ApiResponse.success("Supplier updated successfully", supplierMapper.toResponseDto(updated));
    }

    @DeleteMapping("/{tenantId}/{id}")
    public ApiResponse<Void> delete(
            @PathVariable("tenantId") UUID tenantId,
            @PathVariable("id") UUID id
    ) {
        supplierService.deleteSupplier(id, tenantId);
        return ApiResponse.success("Supplier deleted successfully", null);
    }
}
