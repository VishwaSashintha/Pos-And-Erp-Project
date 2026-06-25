package com.gradge.erp.vehicle.service;

import com.gradge.erp.vehicle.entity.Vehicle;
import com.gradge.erp.vehicle.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VehicleService {

    private final VehicleRepository vehicleRepository;

    public Vehicle createVehicle(Vehicle vehicle) {
        return vehicleRepository.save(vehicle);
    }

    public List<Vehicle> getAllVehicles(UUID tenantId) {
        return vehicleRepository.findByTenant_IdAndDeletedFalse(tenantId);
    }

    public List<Vehicle> getVehiclesByCustomer(UUID customerId, UUID tenantId) {
        return vehicleRepository.findByCustomer_IdAndTenant_Id(customerId, tenantId);
    }

    public Vehicle getVehicle(UUID id, UUID tenantId) {
        return vehicleRepository.findByIdAndTenant_Id(id, tenantId);
    }

    public Vehicle updateVehicle(UUID id, Vehicle updated, UUID tenantId) {
        Vehicle existing = getVehicle(id, tenantId);

        existing.setVehicleNumber(updated.getVehicleNumber());
        existing.setBrand(updated.getBrand());
        existing.setModel(updated.getModel());
        existing.setColor(updated.getColor());
        existing.setFuelType(updated.getFuelType());
        existing.setMileage(updated.getMileage());
        existing.setNextServiceDue(updated.getNextServiceDue());

        return vehicleRepository.save(existing);
    }

    public void deleteVehicle(UUID id, UUID tenantId) {
        Vehicle vehicle = getVehicle(id, tenantId);
        vehicle.setDeleted(true);
        vehicleRepository.save(vehicle);
    }
}
