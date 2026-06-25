package com.gradge.erp.workshop.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
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
public class JobCardRequestDto {

    @NotBlank(message = "Vehicle number is required")
    @Size(max = 20, message = "Vehicle number must not exceed 20 characters")
    private String vehicleNumber;

    private String status;

    @PositiveOrZero(message = "Labor cost must be zero or positive")
    private Double laborCost;

    @PositiveOrZero(message = "Parts cost must be zero or positive")
    private Double partsCost;

    @PositiveOrZero(message = "Total cost must be zero or positive")
    private Double totalCost;

    @NotNull(message = "Customer ID is required")
    private UUID customerId;
}
