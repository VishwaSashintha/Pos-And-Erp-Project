package com.gradge.erp.vehicle.dto;

import com.gradge.erp.customer.entity.Customer;
import com.gradge.erp.vehicle.entity.Vehicle;
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
public class VehicleMapperImpl implements VehicleMapper {

    @Override
    public Vehicle toEntity(VehicleRequestDto dto) {
        if ( dto == null ) {
            return null;
        }

        Vehicle.VehicleBuilder vehicle = Vehicle.builder();

        vehicle.vehicleNumber( dto.getVehicleNumber() );
        vehicle.brand( dto.getBrand() );
        vehicle.model( dto.getModel() );
        vehicle.color( dto.getColor() );
        vehicle.fuelType( dto.getFuelType() );
        vehicle.yearOfManufacture( dto.getYearOfManufacture() );
        vehicle.chassisNumber( dto.getChassisNumber() );
        vehicle.engineNumber( dto.getEngineNumber() );
        vehicle.mileage( dto.getMileage() );

        return vehicle.build();
    }

    @Override
    public VehicleResponseDto toResponseDto(Vehicle entity) {
        if ( entity == null ) {
            return null;
        }

        VehicleResponseDto.VehicleResponseDtoBuilder vehicleResponseDto = VehicleResponseDto.builder();

        vehicleResponseDto.customerId( entityCustomerId( entity ) );
        vehicleResponseDto.customerName( entityCustomerName( entity ) );
        vehicleResponseDto.id( entity.getId() );
        vehicleResponseDto.vehicleNumber( entity.getVehicleNumber() );
        vehicleResponseDto.brand( entity.getBrand() );
        vehicleResponseDto.model( entity.getModel() );
        vehicleResponseDto.color( entity.getColor() );
        vehicleResponseDto.fuelType( entity.getFuelType() );
        vehicleResponseDto.yearOfManufacture( entity.getYearOfManufacture() );
        vehicleResponseDto.chassisNumber( entity.getChassisNumber() );
        vehicleResponseDto.engineNumber( entity.getEngineNumber() );
        vehicleResponseDto.lastServiceDate( entity.getLastServiceDate() );
        vehicleResponseDto.nextServiceDue( entity.getNextServiceDue() );
        vehicleResponseDto.mileage( entity.getMileage() );
        vehicleResponseDto.createdAt( entity.getCreatedAt() );
        vehicleResponseDto.updatedAt( entity.getUpdatedAt() );

        return vehicleResponseDto.build();
    }

    @Override
    public List<VehicleResponseDto> toResponseDtoList(List<Vehicle> entities) {
        if ( entities == null ) {
            return null;
        }

        List<VehicleResponseDto> list = new ArrayList<VehicleResponseDto>( entities.size() );
        for ( Vehicle vehicle : entities ) {
            list.add( toResponseDto( vehicle ) );
        }

        return list;
    }

    @Override
    public void updateEntityFromDto(VehicleRequestDto dto, Vehicle entity) {
        if ( dto == null ) {
            return;
        }

        entity.setVehicleNumber( dto.getVehicleNumber() );
        entity.setBrand( dto.getBrand() );
        entity.setModel( dto.getModel() );
        entity.setColor( dto.getColor() );
        entity.setFuelType( dto.getFuelType() );
        entity.setYearOfManufacture( dto.getYearOfManufacture() );
        entity.setChassisNumber( dto.getChassisNumber() );
        entity.setEngineNumber( dto.getEngineNumber() );
        entity.setMileage( dto.getMileage() );
    }

    private UUID entityCustomerId(Vehicle vehicle) {
        if ( vehicle == null ) {
            return null;
        }
        Customer customer = vehicle.getCustomer();
        if ( customer == null ) {
            return null;
        }
        UUID id = customer.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private String entityCustomerName(Vehicle vehicle) {
        if ( vehicle == null ) {
            return null;
        }
        Customer customer = vehicle.getCustomer();
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
