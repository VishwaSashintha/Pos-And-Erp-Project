package com.gradge.erp.inventory.service;

import com.gradge.erp.inventory.entity.Category;
import com.gradge.erp.inventory.repository.CategoryRepository;
import com.gradge.erp.tenant.service.TenantService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final TenantService tenantService;

    public Category createCategory(Category category) {
        UUID tenantId = tenantService.getCurrentTenantId();
        category.setTenantId(tenantId);
        return categoryRepository.save(category);
    }

    public List<Category> getAllCategories(UUID tenantId) {
        return categoryRepository.findByTenantIdAndDeletedFalse(tenantId);
    }

    public Category getCategory(UUID id, UUID tenantId) {
        Category category = categoryRepository.findByIdAndTenantId(id, tenantId);
        if (category == null || category.isDeleted()) {
            throw new RuntimeException("Category not found");
        }
        return category;
    }

    public Category updateCategory(UUID id, Category updated, UUID tenantId) {
        Category existing = getCategory(id, tenantId);
        existing.setName(updated.getName());
        existing.setDescription(updated.getDescription());
        return categoryRepository.save(existing);
    }

    public void deleteCategory(UUID id, UUID tenantId) {
        Category category = getCategory(id, tenantId);
        category.setDeleted(true);
        categoryRepository.save(category);
    }
}
