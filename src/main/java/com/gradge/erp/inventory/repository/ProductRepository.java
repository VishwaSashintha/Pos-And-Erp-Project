package com.gradge.erp.inventory.repository;

import com.gradge.erp.inventory.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {

    List<Product> findByTenant_IdAndDeletedFalse(UUID tenantId);

    Product findByIdAndTenant_Id(UUID id, UUID tenantId);

    Optional<Product> findByNameAndTenantIdAndDeletedFalse(String name, UUID tenantId);
}
