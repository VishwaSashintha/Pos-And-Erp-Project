package com.gradge.erp.supplier.dto;

import com.gradge.erp.supplier.entity.Supplier;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SupplierMapper {

    Supplier toEntity(SupplierRequestDto dto);

    SupplierResponseDto toResponseDto(Supplier entity);

    List<SupplierResponseDto> toResponseDtoList(List<Supplier> entities);

    void updateEntityFromDto(SupplierRequestDto dto, @MappingTarget Supplier entity);
}
