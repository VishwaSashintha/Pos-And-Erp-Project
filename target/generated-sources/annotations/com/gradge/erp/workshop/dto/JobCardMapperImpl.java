package com.gradge.erp.workshop.dto;

import com.gradge.erp.customer.entity.Customer;
import com.gradge.erp.workshop.entity.JobCard;
import com.gradge.erp.workshop.enums.JobCardStatus;
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
public class JobCardMapperImpl implements JobCardMapper {

    @Override
    public JobCard toEntity(JobCardRequestDto dto) {
        if ( dto == null ) {
            return null;
        }

        JobCard.JobCardBuilder jobCard = JobCard.builder();

        jobCard.vehicleNumber( dto.getVehicleNumber() );
        if ( dto.getStatus() != null ) {
            jobCard.status( Enum.valueOf( JobCardStatus.class, dto.getStatus() ) );
        }
        jobCard.laborCost( dto.getLaborCost() );
        jobCard.partsCost( dto.getPartsCost() );
        jobCard.totalCost( dto.getTotalCost() );

        return jobCard.build();
    }

    @Override
    public JobCardResponseDto toResponseDto(JobCard entity) {
        if ( entity == null ) {
            return null;
        }

        JobCardResponseDto.JobCardResponseDtoBuilder jobCardResponseDto = JobCardResponseDto.builder();

        jobCardResponseDto.customerId( entityCustomerId( entity ) );
        jobCardResponseDto.customerName( entityCustomerName( entity ) );
        jobCardResponseDto.id( entity.getId() );
        jobCardResponseDto.jobNumber( entity.getJobNumber() );
        jobCardResponseDto.vehicleNumber( entity.getVehicleNumber() );
        if ( entity.getStatus() != null ) {
            jobCardResponseDto.status( entity.getStatus().name() );
        }
        jobCardResponseDto.laborCost( entity.getLaborCost() );
        jobCardResponseDto.partsCost( entity.getPartsCost() );
        jobCardResponseDto.totalCost( entity.getTotalCost() );
        jobCardResponseDto.invoiceGenerated( entity.isInvoiceGenerated() );
        jobCardResponseDto.createdAt( entity.getCreatedAt() );
        jobCardResponseDto.updatedAt( entity.getUpdatedAt() );

        return jobCardResponseDto.build();
    }

    @Override
    public List<JobCardResponseDto> toResponseDtoList(List<JobCard> entities) {
        if ( entities == null ) {
            return null;
        }

        List<JobCardResponseDto> list = new ArrayList<JobCardResponseDto>( entities.size() );
        for ( JobCard jobCard : entities ) {
            list.add( toResponseDto( jobCard ) );
        }

        return list;
    }

    @Override
    public void updateEntityFromDto(JobCardRequestDto dto, JobCard entity) {
        if ( dto == null ) {
            return;
        }

        entity.setVehicleNumber( dto.getVehicleNumber() );
        if ( dto.getStatus() != null ) {
            entity.setStatus( Enum.valueOf( JobCardStatus.class, dto.getStatus() ) );
        }
        else {
            entity.setStatus( null );
        }
        entity.setLaborCost( dto.getLaborCost() );
        entity.setPartsCost( dto.getPartsCost() );
        entity.setTotalCost( dto.getTotalCost() );
    }

    private UUID entityCustomerId(JobCard jobCard) {
        if ( jobCard == null ) {
            return null;
        }
        Customer customer = jobCard.getCustomer();
        if ( customer == null ) {
            return null;
        }
        UUID id = customer.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private String entityCustomerName(JobCard jobCard) {
        if ( jobCard == null ) {
            return null;
        }
        Customer customer = jobCard.getCustomer();
        if ( customer == null ) {
            return null;
        }
        String name = customer.getName();
        if ( name == null ) {
            return null;
        }
        return name;
    }
}
