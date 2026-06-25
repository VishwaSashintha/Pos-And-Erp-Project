package com.gradge.erp.inventory.repository;

import com.gradge.erp.inventory.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, UUID> {
    List<Category> findByTenantIdAndDeletedFalse(UUID tenantId);
    Category findByIdAndTenantId(UUID id, UUID tenantId);
}
