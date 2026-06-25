package com.gradge.erp.inventory.repository;

import com.gradge.erp.inventory.entity.StockMovement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface StockMovementRepository extends JpaRepository<StockMovement, UUID> {

    List<StockMovement> findByTenant_Id(UUID tenantId);
    List<StockMovement> findByProduct_IdAndTenant_Id(UUID productId, UUID tenantId);
}
