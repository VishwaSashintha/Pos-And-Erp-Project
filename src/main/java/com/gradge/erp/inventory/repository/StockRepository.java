package com.gradge.erp.inventory.repository;

import com.gradge.erp.inventory.entity.Stock;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StockRepository extends JpaRepository<Stock, UUID> {
    List<Stock> findByProduct_IdAndTenant_Id(UUID productId, UUID tenantId);
    Optional<Stock> findByProduct_IdAndWarehouse_IdAndTenant_Id(UUID productId, UUID warehouseId, UUID tenantId);
    Optional<Stock> findByProduct_IdAndWarehouseIsNullAndTenant_Id(UUID productId, UUID tenantId);
}
