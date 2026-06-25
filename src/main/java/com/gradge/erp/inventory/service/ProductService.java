package com.gradge.erp.inventory.service;

import com.gradge.erp.common.audit.Auditable;
import com.gradge.erp.inventory.entity.Product;
import com.gradge.erp.inventory.repository.ProductRepository;
import com.gradge.erp.tenant.service.TenantService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final TenantService tenantService;

    @Auditable(action = "PRODUCT_CREATED")
    public Product createProduct(Product product) {
        UUID tenantId = tenantService.getCurrentTenantId();
        product.setTenantId(tenantId);
        if (product.getQuantity() == null) {
            product.setQuantity(0);
        }
        return productRepository.save(product);
    }

    public List<Product> getAllProducts(UUID tenantId) {
        return productRepository.findByTenant_IdAndDeletedFalse(tenantId);
    }

    public Product getProduct(UUID id, UUID tenantId) {
        Product product = productRepository.findByIdAndTenant_Id(id, tenantId);
        if (product == null) {
            throw new RuntimeException("Product not found");
        }
        return product;
    }

    @Auditable(action = "PRODUCT_UPDATED")
    public Product updateProduct(UUID id, Product updated, UUID tenantId) {
        Product existing = getProduct(id, tenantId);
        existing.setName(updated.getName());
        existing.setSku(updated.getSku());
        existing.setBarcode(updated.getBarcode());
        existing.setSellingPrice(updated.getSellingPrice());
        existing.setCostPrice(updated.getCostPrice());
        existing.setQuantity(updated.getQuantity());
        existing.setReorderLevel(updated.getReorderLevel());
        existing.setCategory(updated.getCategory());
        return productRepository.save(existing);
    }

    @Auditable(action = "PRODUCT_DELETED")
    public void deleteProduct(UUID id, UUID tenantId) {
        Product product = getProduct(id, tenantId);
        product.setDeleted(true);
        productRepository.save(product);
    }

    public List<Product> getLowStockProducts(UUID tenantId) {
        List<Product> products = getAllProducts(tenantId);
        return products.stream()
                .filter(p -> p.getQuantity() != null && p.getReorderLevel() != null && p.getQuantity() <= p.getReorderLevel())
                .toList();
    }
}