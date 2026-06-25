package com.gradge.erp.vehicle.repository;

import com.gradge.erp.vehicle.entity.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface VehicleRepository extends JpaRepository<Vehicle, UUID> {

    List<Vehicle> findByTenant_IdAndDeletedFalse(UUID tenantId);

    List<Vehicle> findByCustomer_IdAndTenant_Id(UUID customerId, UUID tenantId);

    Vehicle findByIdAndTenant_Id(UUID id, UUID tenantId);

    Vehicle findByVehicleNumberAndTenant_Id(String vehicleNumber, UUID tenantId);
}
