package com.gradge.erp.inventory.dto;

import com.gradge.erp.inventory.entity.Category;
import com.gradge.erp.inventory.entity.Product;
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
public class ProductMapperImpl implements ProductMapper {

    @Override
    public Product toEntity(ProductRequestDto dto) {
        if ( dto == null ) {
            return null;
        }

        Product.ProductBuilder product = Product.builder();

        product.name( dto.getName() );
        product.sku( dto.getSku() );
        product.barcode( dto.getBarcode() );
        product.sellingPrice( dto.getSellingPrice() );
        product.costPrice( dto.getCostPrice() );
        product.quantity( dto.getQuantity() );
        product.reorderLevel( dto.getReorderLevel() );

        return product.build();
    }

    @Override
    public ProductResponseDto toResponseDto(Product entity) {
        if ( entity == null ) {
            return null;
        }

        ProductResponseDto.ProductResponseDtoBuilder productResponseDto = ProductResponseDto.builder();

        productResponseDto.categoryId( entityCategoryId( entity ) );
        productResponseDto.categoryName( entityCategoryName( entity ) );
        productResponseDto.id( entity.getId() );
        productResponseDto.name( entity.getName() );
        productResponseDto.sku( entity.getSku() );
        productResponseDto.barcode( entity.getBarcode() );
        productResponseDto.sellingPrice( entity.getSellingPrice() );
        productResponseDto.costPrice( entity.getCostPrice() );
        productResponseDto.quantity( entity.getQuantity() );
        productResponseDto.reorderLevel( entity.getReorderLevel() );
        productResponseDto.createdAt( entity.getCreatedAt() );
        productResponseDto.updatedAt( entity.getUpdatedAt() );

        return productResponseDto.build();
    }

    @Override
    public List<ProductResponseDto> toResponseDtoList(List<Product> entities) {
        if ( entities == null ) {
            return null;
        }

        List<ProductResponseDto> list = new ArrayList<ProductResponseDto>( entities.size() );
        for ( Product product : entities ) {
            list.add( toResponseDto( product ) );
        }

        return list;
    }

    @Override
    public void updateEntityFromDto(ProductRequestDto dto, Product entity) {
        if ( dto == null ) {
            return;
        }

        entity.setName( dto.getName() );
        entity.setSku( dto.getSku() );
        entity.setBarcode( dto.getBarcode() );
        entity.setSellingPrice( dto.getSellingPrice() );
        entity.setCostPrice( dto.getCostPrice() );
        entity.setQuantity( dto.getQuantity() );
        entity.setReorderLevel( dto.getReorderLevel() );
    }

    private UUID entityCategoryId(Product product) {
        if ( product == null ) {
            return null;
        }
        Category category = product.getCategory();
        if ( category == null ) {
            return null;
        }
        UUID id = category.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private String entityCategoryName(Product product) {
        if ( product == null ) {
            return null;
        }
        Category category = product.getCategory();
        if ( category == null ) {
            return null;
        }
        String name = category.getName();
        if ( name == null ) {
            return null;
        }
        return name;
    }
}
