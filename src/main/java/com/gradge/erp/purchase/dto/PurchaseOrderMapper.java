package com.gradge.erp.purchase.dto;

import com.gradge.erp.purchase.entity.PurchaseOrder;
import com.gradge.erp.purchase.entity.PurchaseOrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PurchaseOrderMapper {

    @Mapping(source = "supplier.id", target = "supplierId")
    @Mapping(source = "supplier.name", target = "supplierName")
    PurchaseOrderResponseDto toResponseDto(PurchaseOrder entity);

    List<PurchaseOrderResponseDto> toResponseDtoList(List<PurchaseOrder> entities);

    @Mapping(source = "product.id", target = "productId")
    @Mapping(source = "product.name", target = "productName")
    PurchaseOrderResponseDto.PurchaseOrderItemResponseDto toItemResponseDto(PurchaseOrderItem item);

    List<PurchaseOrderResponseDto.PurchaseOrderItemResponseDto> toItemResponseDtoList(List<PurchaseOrderItem> items);
}
