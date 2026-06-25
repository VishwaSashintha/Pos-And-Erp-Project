package com.gradge.erp.inventory.dto;

import com.gradge.erp.inventory.entity.Category;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CategoryMapper {

    Category toEntity(CategoryRequestDto dto);

    CategoryResponseDto toResponseDto(Category entity);

    List<CategoryResponseDto> toResponseDtoList(List<Category> entities);

    void updateEntityFromDto(CategoryRequestDto dto, @MappingTarget Category entity);
}
