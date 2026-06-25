package com.gradge.erp.workshop.dto;

import com.gradge.erp.workshop.entity.Inspection;
import com.gradge.erp.workshop.entity.InspectionItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface InspectionMapper {

    @Mapping(source = "jobCard.id", target = "jobCardId")
    @Mapping(source = "tenant.id", target = "tenantId")
    InspectionResponseDto toResponseDto(Inspection entity);

    List<InspectionResponseDto> toResponseDtoList(List<Inspection> entities);

    @Mapping(source = "inspection.id", target = "inspectionId")
    InspectionItemResponseDto toResponseDto(InspectionItem entity);

    List<InspectionItemResponseDto> toItemResponseDtoList(List<InspectionItem> entities);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "inspection", ignore = true)
    InspectionItem toEntity(InspectionItemRequestDto dto);
}
