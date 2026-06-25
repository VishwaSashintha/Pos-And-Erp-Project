package com.gradge.erp.asset.service;

import com.gradge.erp.asset.entity.Asset;
import com.gradge.erp.asset.enums.AssetStatus;
import com.gradge.erp.asset.repository.AssetRepository;
import com.gradge.erp.auth.entity.Employee;
import com.gradge.erp.auth.repository.EmployeeRepository;
import com.gradge.erp.tenant.entity.Tenant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AssetService {

    private final AssetRepository assetRepository;
    private final EmployeeRepository employeeRepository;

    public Asset createAsset(Asset asset, Tenant tenant) {
        asset.setTenantId(tenant.getId());
        asset.setStatus(AssetStatus.AVAILABLE);
        return assetRepository.save(asset);
    }

    public Asset assignAsset(UUID assetId, UUID employeeId, Tenant tenant) {
        Asset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new RuntimeException("Asset not found"));

        if (!asset.getTenantId().equals(tenant.getId())) {
            throw new RuntimeException("Unauthorized access to asset");
        }

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        if (!employee.getTenantId().equals(tenant.getId())) {
            throw new RuntimeException("Unauthorized access to employee");
        }

        asset.setAssignedTo(employee);
        asset.setStatus(AssetStatus.ASSIGNED);
        return assetRepository.save(asset);
    }

    public Asset returnAsset(UUID assetId, Tenant tenant) {
        Asset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new RuntimeException("Asset not found"));

        if (!asset.getTenantId().equals(tenant.getId())) {
            throw new RuntimeException("Unauthorized access to asset");
        }

        asset.setAssignedTo(null);
        asset.setStatus(AssetStatus.AVAILABLE);
        return assetRepository.save(asset);
    }

    public List<Asset> getAssetsByTenant(Tenant tenant) {
        return assetRepository.findByTenantId(tenant.getId());
    }
}
