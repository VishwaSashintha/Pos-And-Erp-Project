package com.gradge.erp.inventory.repository;

import com.gradge.erp.inventory.entity.StockTransfer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface StockTransferRepository extends JpaRepository<StockTransfer, UUID> {
    List<StockTransfer> findByTenantId(UUID tenantId);
    List<StockTransfer> findBySourceWarehouseIdOrDestinationWarehouseId(UUID sourceId, UUID destId);
}
