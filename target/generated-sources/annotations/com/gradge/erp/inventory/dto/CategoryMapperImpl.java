package com.gradge.erp.inventory.dto;

import com.gradge.erp.inventory.entity.Category;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-06-23T14:17:42+0530",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 25.0.2 (Oracle Corporation)"
)
@Component
public class CategoryMapperImpl implements CategoryMapper {

    @Override
    public Category toEntity(CategoryRequestDto dto) {
        if ( dto == null ) {
            return null;
        }

        Category.CategoryBuilder category = Category.builder();

        category.name( dto.getName() );
        category.description( dto.getDescription() );

        return category.build();
    }

    @Override
    public CategoryResponseDto toResponseDto(Category entity) {
        if ( entity == null ) {
            return null;
        }

        CategoryResponseDto.CategoryResponseDtoBuilder categoryResponseDto = CategoryResponseDto.builder();

        categoryResponseDto.id( entity.getId() );
        categoryResponseDto.tenantId( entity.getTenantId() );
        categoryResponseDto.name( entity.getName() );
        categoryResponseDto.description( entity.getDescription() );
        categoryResponseDto.createdAt( entity.getCreatedAt() );
        categoryResponseDto.updatedAt( entity.getUpdatedAt() );

        return categoryResponseDto.build();
    }

    @Override
    public List<CategoryResponseDto> toResponseDtoList(List<Category> entities) {
        if ( entities == null ) {
            return null;
        }

        List<CategoryResponseDto> list = new ArrayList<CategoryResponseDto>( entities.size() );
        for ( Category category : entities ) {
            list.add( toResponseDto( category ) );
        }

        return list;
    }

    @Override
    public void updateEntityFromDto(CategoryRequestDto dto, Category entity) {
        if ( dto == null ) {
            return;
        }

        entity.setName( dto.getName() );
        entity.setDescription( dto.getDescription() );
    }
}
