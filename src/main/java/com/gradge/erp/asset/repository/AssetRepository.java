package com.gradge.erp.asset.repository;

import com.gradge.erp.asset.entity.Asset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AssetRepository extends JpaRepository<Asset, UUID> {
    List<Asset> findByTenantId(UUID tenantId);
    List<Asset> findByAssignedTo_IdAndTenantId(UUID employeeId, UUID tenantId);
}
