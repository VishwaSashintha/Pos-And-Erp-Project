package com.gradge.erp.vehicle.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VehicleRequestDto {

    @NotBlank(message = "Vehicle number is required")
    @Size(max = 20, message = "Vehicle number must not exceed 20 characters")
    private String vehicleNumber;

    @Size(max = 100, message = "Brand must not exceed 100 characters")
    private String brand;

    @Size(max = 100, message = "Model must not exceed 100 characters")
    private String model;

    @Size(max = 50, message = "Color must not exceed 50 characters")
    private String color;

    @Size(max = 20, message = "Fuel type must not exceed 20 characters")
    private String fuelType;

    private Integer yearOfManufacture;

    @Size(max = 50, message = "Chassis number must not exceed 50 characters")
    private String chassisNumber;

    @Size(max = 50, message = "Engine number must not exceed 50 characters")
    private String engineNumber;

    @Positive(message = "Mileage must be positive")
    private Integer mileage;

    @NotNull(message = "Customer ID is required")
    private UUID customerId;
}
