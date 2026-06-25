package com.gradge.erp.inventory.dto;

import com.gradge.erp.inventory.entity.Product;
import com.gradge.erp.inventory.entity.StockMovement;
import com.gradge.erp.tenant.entity.Tenant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-06-26T01:47:14+0530",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 25.0.2 (Oracle Corporation)"
)
@Component
public class StockMovementMapperImpl implements StockMovementMapper {

    @Override
    public StockMovementResponseDto toResponseDto(StockMovement entity) {
        if ( entity == null ) {
            return null;
        }

        StockMovementResponseDto.StockMovementResponseDtoBuilder stockMovementResponseDto = StockMovementResponseDto.builder();

        stockMovementResponseDto.productId( entityProductId( entity ) );
        stockMovementResponseDto.productName( entityProductName( entity ) );
        stockMovementResponseDto.tenantId( entityTenantId( entity ) );
        stockMovementResponseDto.id( entity.getId() );
        stockMovementResponseDto.type( entity.getType() );
        stockMovementResponseDto.quantity( entity.getQuantity() );
        stockMovementResponseDto.reference( entity.getReference() );
        stockMovementResponseDto.createdAt( entity.getCreatedAt() );

        return stockMovementResponseDto.build();
    }

    @Override
    public List<StockMovementResponseDto> toResponseDtoList(List<StockMovement> entities) {
        if ( entities == null ) {
            return null;
        }

        List<StockMovementResponseDto> list = new ArrayList<StockMovementResponseDto>( entities.size() );
        for ( StockMovement stockMovement : entities ) {
            list.add( toResponseDto( stockMovement ) );
        }

        return list;
    }

    private UUID entityProductId(StockMovement stockMovement) {
        if ( stockMovement == null ) {
            return null;
        }
        Product product = stockMovement.getProduct();
        if ( product == null ) {
            return null;
        }
        UUID id = product.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private String entityProductName(StockMovement stockMovement) {
        if ( stockMovement == null ) {
            return null;
        }
        Product product = stockMovement.getProduct();
        if ( product == null ) {
            return null;
        }
        String name = product.getName();
        if ( name == null ) {
            return null;
        }
        return name;
    }

    private UUID entityTenantId(StockMovement stockMovement) {
        if ( stockMovement == null ) {
            return null;
        }
        Tenant tenant = stockMovement.getTenant();
        if ( tenant == null ) {
            return null;
        }
        UUID id = tenant.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }
}
