package com.gradge.erp.supplier.repository;

import com.gradge.erp.supplier.entity.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface SupplierRepository extends JpaRepository<Supplier, UUID> {
    List<Supplier> findByTenantIdAndDeletedFalse(UUID tenantId);
    Supplier findByIdAndTenantId(UUID id, UUID tenantId);
    List<Supplier> findByTenantIdAndDeletedFalseAndNameContainingIgnoreCase(UUID tenantId, String name);
}
