package com.gradge.erp.asset.controller;

import com.gradge.erp.asset.entity.Asset;
import com.gradge.erp.asset.service.AssetService;
import com.gradge.erp.common.response.ApiResponse;
import com.gradge.erp.tenant.context.TenantContext;
import com.gradge.erp.tenant.entity.Tenant;
import com.gradge.erp.tenant.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/assets")
@RequiredArgsConstructor
public class AssetController {

    private final AssetService assetService;
    private final TenantRepository tenantRepository;

    private Tenant getCurrentTenant() {
        return tenantRepository.findById(TenantContext.getTenantId())
                .orElseThrow(() -> new RuntimeException("Tenant not found"));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('MANAGE_ASSETS')")
    public ApiResponse<Asset> createAsset(@RequestBody Asset asset) {
        return ApiResponse.success("Asset created", assetService.createAsset(asset, getCurrentTenant()));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('VIEW_ASSETS')")
    public ApiResponse<List<Asset>> getAssets() {
        return ApiResponse.success("Fetched assets", assetService.getAssetsByTenant(getCurrentTenant()));
    }

    @PostMapping("/{assetId}/assign/{employeeId}")
    @PreAuthorize("hasAuthority('MANAGE_ASSETS')")
    public ApiResponse<Asset> assignAsset(@PathVariable UUID assetId, @PathVariable UUID employeeId) {
        return ApiResponse.success("Asset assigned", assetService.assignAsset(assetId, employeeId, getCurrentTenant()));
    }

    @PostMapping("/{assetId}/return")
    @PreAuthorize("hasAuthority('MANAGE_ASSETS')")
    public ApiResponse<Asset> returnAsset(@PathVariable UUID assetId) {
        return ApiResponse.success("Asset returned", assetService.returnAsset(assetId, getCurrentTenant()));
    }
}
