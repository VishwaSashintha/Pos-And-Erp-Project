package com.gradge.erp.vehicle.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VehicleResponseDto {

    private UUID id;
    private String vehicleNumber;
    private String brand;
    private String model;
    private String color;
    private String fuelType;
    private Integer yearOfManufacture;
    private String chassisNumber;
    private String engineNumber;
    private LocalDate lastServiceDate;
    private LocalDate nextServiceDue;
    private Integer mileage;
    private UUID customerId;
    private String customerName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
