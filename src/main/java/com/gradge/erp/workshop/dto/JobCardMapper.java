package com.gradge.erp.workshop.dto;

import com.gradge.erp.workshop.entity.JobCard;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface JobCardMapper {

    JobCard toEntity(JobCardRequestDto dto);

    @Mapping(source = "customer.id", target = "customerId")
    @Mapping(source = "customer.name", target = "customerName")
    JobCardResponseDto toResponseDto(JobCard entity);

    List<JobCardResponseDto> toResponseDtoList(List<JobCard> entities);

    void updateEntityFromDto(JobCardRequestDto dto, @MappingTarget JobCard entity);
}
