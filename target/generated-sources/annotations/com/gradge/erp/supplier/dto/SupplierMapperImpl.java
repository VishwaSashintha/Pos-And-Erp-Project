package com.gradge.erp.supplier.dto;

import com.gradge.erp.supplier.entity.Supplier;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-06-26T01:47:14+0530",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 25.0.2 (Oracle Corporation)"
)
@Component
public class SupplierMapperImpl implements SupplierMapper {

    @Override
    public Supplier toEntity(SupplierRequestDto dto) {
        if ( dto == null ) {
            return null;
        }

        Supplier.SupplierBuilder supplier = Supplier.builder();

        supplier.name( dto.getName() );
        supplier.contactName( dto.getContactName() );
        supplier.phone( dto.getPhone() );
        supplier.email( dto.getEmail() );
        supplier.address( dto.getAddress() );
        supplier.taxNumber( dto.getTaxNumber() );

        return supplier.build();
    }

    @Override
    public SupplierResponseDto toResponseDto(Supplier entity) {
        if ( entity == null ) {
            return null;
        }

        SupplierResponseDto.SupplierResponseDtoBuilder supplierResponseDto = SupplierResponseDto.builder();

        supplierResponseDto.id( entity.getId() );
        supplierResponseDto.name( entity.getName() );
        supplierResponseDto.contactName( entity.getContactName() );
        supplierResponseDto.phone( entity.getPhone() );
        supplierResponseDto.email( entity.getEmail() );
        supplierResponseDto.address( entity.getAddress() );
        supplierResponseDto.taxNumber( entity.getTaxNumber() );
        supplierResponseDto.createdAt( entity.getCreatedAt() );
        supplierResponseDto.updatedAt( entity.getUpdatedAt() );

        return supplierResponseDto.build();
    }

    @Override
    public List<SupplierResponseDto> toResponseDtoList(List<Supplier> entities) {
        if ( entities == null ) {
            return null;
        }

        List<SupplierResponseDto> list = new ArrayList<SupplierResponseDto>( entities.size() );
        for ( Supplier supplier : entities ) {
            list.add( toResponseDto( supplier ) );
        }

        return list;
    }

    @Override
    public void updateEntityFromDto(SupplierRequestDto dto, Supplier entity) {
        if ( dto == null ) {
            return;
        }

        entity.setName( dto.getName() );
        entity.setContactName( dto.getContactName() );
        entity.setPhone( dto.getPhone() );
        entity.setEmail( dto.getEmail() );
        entity.setAddress( dto.getAddress() );
        entity.setTaxNumber( dto.getTaxNumber() );
    }
}
