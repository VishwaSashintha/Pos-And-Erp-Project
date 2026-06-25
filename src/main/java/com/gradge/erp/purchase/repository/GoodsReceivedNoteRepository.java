package com.gradge.erp.purchase.repository;

import com.gradge.erp.purchase.entity.GoodsReceivedNote;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface GoodsReceivedNoteRepository extends JpaRepository<GoodsReceivedNote, UUID> {
    List<GoodsReceivedNote> findByTenantIdAndDeletedFalse(UUID tenantId);
    List<GoodsReceivedNote> findByPurchaseOrder_IdAndDeletedFalse(UUID purchaseOrderId);
}
