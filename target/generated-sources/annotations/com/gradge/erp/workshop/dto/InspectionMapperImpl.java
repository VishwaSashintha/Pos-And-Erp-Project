package com.gradge.erp.workshop.dto;

import com.gradge.erp.tenant.entity.Tenant;
import com.gradge.erp.workshop.entity.Inspection;
import com.gradge.erp.workshop.entity.InspectionItem;
import com.gradge.erp.workshop.entity.JobCard;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-06-23T14:17:42+0530",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 25.0.2 (Oracle Corporation)"
)
@Component
public class InspectionMapperImpl implements InspectionMapper {

    @Override
    public InspectionResponseDto toResponseDto(Inspection entity) {
        if ( entity == null ) {
            return null;
        }

        InspectionResponseDto.InspectionResponseDtoBuilder inspectionResponseDto = InspectionResponseDto.builder();

        inspectionResponseDto.jobCardId( entityJobCardId( entity ) );
        inspectionResponseDto.tenantId( entityTenantId( entity ) );
        inspectionResponseDto.id( entity.getId() );
        inspectionResponseDto.status( entity.getStatus() );
        inspectionResponseDto.overallNotes( entity.getOverallNotes() );
        inspectionResponseDto.estimatedCost( entity.getEstimatedCost() );
        inspectionResponseDto.branchId( entity.getBranchId() );
        inspectionResponseDto.createdAt( entity.getCreatedAt() );
        inspectionResponseDto.updatedAt( entity.getUpdatedAt() );

        return inspectionResponseDto.build();
    }

    @Override
    public List<InspectionResponseDto> toResponseDtoList(List<Inspection> entities) {
        if ( entities == null ) {
            return null;
        }

        List<InspectionResponseDto> list = new ArrayList<InspectionResponseDto>( entities.size() );
        for ( Inspection inspection : entities ) {
            list.add( toResponseDto( inspection ) );
        }

        return list;
    }

    @Override
    public InspectionItemResponseDto toResponseDto(InspectionItem entity) {
        if ( entity == null ) {
            return null;
        }

        InspectionItemResponseDto.InspectionItemResponseDtoBuilder inspectionItemResponseDto = InspectionItemResponseDto.builder();

        inspectionItemResponseDto.inspectionId( entityInspectionId( entity ) );
        inspectionItemResponseDto.id( entity.getId() );
        inspectionItemResponseDto.itemName( entity.getItemName() );
        inspectionItemResponseDto.checked( entity.isChecked() );
        inspectionItemResponseDto.remarks( entity.getRemarks() );

        return inspectionItemResponseDto.build();
    }

    @Override
    public List<InspectionItemResponseDto> toItemResponseDtoList(List<InspectionItem> entities) {
        if ( entities == null ) {
            return null;
        }

        List<InspectionItemResponseDto> list = new ArrayList<InspectionItemResponseDto>( entities.size() );
        for ( InspectionItem inspectionItem : entities ) {
            list.add( toResponseDto( inspectionItem ) );
        }

        return list;
    }

    @Override
    public InspectionItem toEntity(InspectionItemRequestDto dto) {
        if ( dto == null ) {
            return null;
        }

        InspectionItem.InspectionItemBuilder inspectionItem = InspectionItem.builder();

        inspectionItem.itemName( dto.getItemName() );
        inspectionItem.checked( dto.isChecked() );
        inspectionItem.remarks( dto.getRemarks() );

        return inspectionItem.build();
    }

    private UUID entityJobCardId(Inspection inspection) {
        if ( inspection == null ) {
            return null;
        }
        JobCard jobCard = inspection.getJobCard();
        if ( jobCard == null ) {
            return null;
        }
        UUID id = jobCard.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private UUID entityTenantId(Inspection inspection) {
        if ( inspection == null ) {
            return null;
        }
        Tenant tenant = inspection.getTenant();
        if ( tenant == null ) {
            return null;
        }
        UUID id = tenant.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private UUID entityInspectionId(InspectionItem inspectionItem) {
        if ( inspectionItem == null ) {
            return null;
        }
        Inspection inspection = inspectionItem.getInspection();
        if ( inspection == null ) {
            return null;
        }
        UUID id = inspection.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }
}
