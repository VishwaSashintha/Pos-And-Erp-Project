package com.gradge.erp.purchase.repository;

import com.gradge.erp.purchase.entity.PurchaseOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, UUID> {
    List<PurchaseOrder> findByTenantIdAndDeletedFalse(UUID tenantId);
    PurchaseOrder findByIdAndTenantId(UUID id, UUID tenantId);
}
