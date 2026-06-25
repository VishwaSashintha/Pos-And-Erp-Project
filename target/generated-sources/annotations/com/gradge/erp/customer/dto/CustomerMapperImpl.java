package com.gradge.erp.customer.dto;

import com.gradge.erp.customer.entity.Customer;
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
public class CustomerMapperImpl implements CustomerMapper {

    @Override
    public Customer toEntity(CustomerRequestDto dto) {
        if ( dto == null ) {
            return null;
        }

        Customer.CustomerBuilder customer = Customer.builder();

        customer.name( dto.getName() );
        customer.phone( dto.getPhone() );
        customer.email( dto.getEmail() );
        customer.address( dto.getAddress() );
        customer.nic( dto.getNic() );

        return customer.build();
    }

    @Override
    public CustomerResponseDto toResponseDto(Customer entity) {
        if ( entity == null ) {
            return null;
        }

        CustomerResponseDto.CustomerResponseDtoBuilder customerResponseDto = CustomerResponseDto.builder();

        customerResponseDto.id( entity.getId() );
        customerResponseDto.name( entity.getName() );
        customerResponseDto.phone( entity.getPhone() );
        customerResponseDto.email( entity.getEmail() );
        customerResponseDto.address( entity.getAddress() );
        customerResponseDto.nic( entity.getNic() );
        customerResponseDto.totalSpent( entity.getTotalSpent() );
        customerResponseDto.visitCount( entity.getVisitCount() );
        customerResponseDto.createdAt( entity.getCreatedAt() );
        customerResponseDto.updatedAt( entity.getUpdatedAt() );

        return customerResponseDto.build();
    }

    @Override
    public List<CustomerResponseDto> toResponseDtoList(List<Customer> entities) {
        if ( entities == null ) {
            return null;
        }

        List<CustomerResponseDto> list = new ArrayList<CustomerResponseDto>( entities.size() );
        for ( Customer customer : entities ) {
            list.add( toResponseDto( customer ) );
        }

        return list;
    }

    @Override
    public void updateEntityFromDto(CustomerRequestDto dto, Customer entity) {
        if ( dto == null ) {
            return;
        }

        entity.setName( dto.getName() );
        entity.setPhone( dto.getPhone() );
        entity.setEmail( dto.getEmail() );
        entity.setAddress( dto.getAddress() );
        entity.setNic( dto.getNic() );
    }
}
