package com.gradge.erp.inventory.dto;

import com.gradge.erp.inventory.entity.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProductMapper {

    Product toEntity(ProductRequestDto dto);

    @Mapping(source = "category.id", target = "categoryId")
    @Mapping(source = "category.name", target = "categoryName")
    ProductResponseDto toResponseDto(Product entity);

    List<ProductResponseDto> toResponseDtoList(List<Product> entities);

    void updateEntityFromDto(ProductRequestDto dto, @MappingTarget Product entity);
}
