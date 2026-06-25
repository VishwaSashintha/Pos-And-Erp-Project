package com.gradge.erp.vehicle.controller;

import com.gradge.erp.common.response.ApiResponse;
import com.gradge.erp.vehicle.dto.VehicleMapper;
import com.gradge.erp.vehicle.dto.VehicleRequestDto;
import com.gradge.erp.vehicle.dto.VehicleResponseDto;
import com.gradge.erp.vehicle.entity.Vehicle;
import com.gradge.erp.vehicle.service.VehicleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/vehicles")
@RequiredArgsConstructor
public class VehicleController {

    private final VehicleService vehicleService;
    private final VehicleMapper vehicleMapper;

    @PostMapping
    public ApiResponse<VehicleResponseDto> create(@Valid @RequestBody VehicleRequestDto dto) {
        Vehicle entity = vehicleMapper.toEntity(dto);
        Vehicle saved = vehicleService.createVehicle(entity);
        return ApiResponse.success("Vehicle registered successfully", vehicleMapper.toResponseDto(saved));
    }

    @GetMapping("/{tenantId}")
    public ApiResponse<List<VehicleResponseDto>> getAll(@PathVariable("tenantId") UUID tenantId) {
        List<Vehicle> vehicles = vehicleService.getAllVehicles(tenantId);
        return ApiResponse.success(vehicleMapper.toResponseDtoList(vehicles));
    }

    @GetMapping("/{tenantId}/customer/{customerId}")
    public ApiResponse<List<VehicleResponseDto>> getByCustomer(
            @PathVariable("tenantId") UUID tenantId,
            @PathVariable("customerId") UUID customerId
    ) {
        List<Vehicle> vehicles = vehicleService.getVehiclesByCustomer(customerId, tenantId);
        return ApiResponse.success(vehicleMapper.toResponseDtoList(vehicles));
    }

    @GetMapping("/{tenantId}/{id}")
    public ApiResponse<VehicleResponseDto> get(
            @PathVariable("tenantId") UUID tenantId,
            @PathVariable("id") UUID id
    ) {
        Vehicle vehicle = vehicleService.getVehicle(id, tenantId);
        return ApiResponse.success(vehicleMapper.toResponseDto(vehicle));
    }

    @PutMapping("/{tenantId}/{id}")
    public ApiResponse<VehicleResponseDto> update(
            @PathVariable("tenantId") UUID tenantId,
            @PathVariable("id") UUID id,
            @Valid @RequestBody VehicleRequestDto dto
    ) {
        Vehicle entity = vehicleMapper.toEntity(dto);
        Vehicle updated = vehicleService.updateVehicle(id, entity, tenantId);
        return ApiResponse.success("Vehicle updated successfully", vehicleMapper.toResponseDto(updated));
    }

    @DeleteMapping("/{tenantId}/{id}")
    public ApiResponse<Void> delete(
            @PathVariable("tenantId") UUID tenantId,
            @PathVariable("id") UUID id
    ) {
        vehicleService.deleteVehicle(id, tenantId);
        return ApiResponse.success("Vehicle deleted successfully", null);
    }
}
