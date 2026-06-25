package com.gradge.erp.inventory.controller;

import com.gradge.erp.common.response.ApiResponse;
import com.gradge.erp.inventory.dto.CategoryMapper;
import com.gradge.erp.inventory.dto.CategoryRequestDto;
import com.gradge.erp.inventory.dto.CategoryResponseDto;
import com.gradge.erp.inventory.entity.Category;
import com.gradge.erp.inventory.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('MANAGE_PRODUCTS')")
public class CategoryController {

    private final CategoryService categoryService;
    private final CategoryMapper categoryMapper;

    @PostMapping
    public ApiResponse<CategoryResponseDto> create(@Valid @RequestBody CategoryRequestDto categoryRequestDto) {
        Category category = categoryMapper.toEntity(categoryRequestDto);
        Category savedCategory = categoryService.createCategory(category);
        return ApiResponse.success("Category created successfully", categoryMapper.toResponseDto(savedCategory));
    }

    @GetMapping("/{tenantId}")
    public ApiResponse<List<CategoryResponseDto>> getAll(@PathVariable("tenantId") UUID tenantId) {
        List<Category> categories = categoryService.getAllCategories(tenantId);
        return ApiResponse.success("Categories retrieved successfully", categoryMapper.toResponseDtoList(categories));
    }

    @GetMapping("/{tenantId}/{id}")
    public ApiResponse<CategoryResponseDto> get(@PathVariable("tenantId") UUID tenantId, @PathVariable("id") UUID id) {
        Category category = categoryService.getCategory(id, tenantId);
        return ApiResponse.success("Category retrieved successfully", categoryMapper.toResponseDto(category));
    }

    @PutMapping("/{tenantId}/{id}")
    public ApiResponse<CategoryResponseDto> update(
            @PathVariable("tenantId") UUID tenantId,
            @PathVariable("id") UUID id,
            @Valid @RequestBody CategoryRequestDto categoryRequestDto
    ) {
        Category category = categoryService.getCategory(id, tenantId);
        categoryMapper.updateEntityFromDto(categoryRequestDto, category);
        Category updatedCategory = categoryService.updateCategory(id, category, tenantId);
        return ApiResponse.success("Category updated successfully", categoryMapper.toResponseDto(updatedCategory));
    }

    @DeleteMapping("/{tenantId}/{id}")
    public ApiResponse<Void> delete(@PathVariable("tenantId") UUID tenantId, @PathVariable("id") UUID id) {
        categoryService.deleteCategory(id, tenantId);
        return ApiResponse.success("Category deleted successfully", null);
    }
}
