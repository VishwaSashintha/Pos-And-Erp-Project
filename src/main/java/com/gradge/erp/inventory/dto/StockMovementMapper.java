package com.gradge.erp.inventory.dto;

import com.gradge.erp.inventory.entity.StockMovement;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface StockMovementMapper {

    @Mapping(source = "product.id", target = "productId")
    @Mapping(source = "product.name", target = "productName")
    @Mapping(source = "tenant.id", target = "tenantId")
    StockMovementResponseDto toResponseDto(StockMovement entity);

    List<StockMovementResponseDto> toResponseDtoList(List<StockMovement> entities);
}
