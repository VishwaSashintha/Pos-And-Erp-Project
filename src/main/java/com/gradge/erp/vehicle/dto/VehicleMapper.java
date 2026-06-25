package com.gradge.erp.vehicle.dto;

import com.gradge.erp.vehicle.entity.Vehicle;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface VehicleMapper {

    Vehicle toEntity(VehicleRequestDto dto);

    @Mapping(source = "customer.id", target = "customerId")
    @Mapping(source = "customer.name", target = "customerName")
    VehicleResponseDto toResponseDto(Vehicle entity);

    List<VehicleResponseDto> toResponseDtoList(List<Vehicle> entities);

    void updateEntityFromDto(VehicleRequestDto dto, @MappingTarget Vehicle entity);
}
