package com.gradge.erp.customer.dto;

import com.gradge.erp.customer.entity.Customer;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CustomerMapper {

    Customer toEntity(CustomerRequestDto dto);

    CustomerResponseDto toResponseDto(Customer entity);

    List<CustomerResponseDto> toResponseDtoList(List<Customer> entities);

    void updateEntityFromDto(CustomerRequestDto dto, @MappingTarget Customer entity);
}
