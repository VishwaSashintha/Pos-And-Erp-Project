package com.gradge.erp.vehicle.entity;

import com.gradge.erp.common.entity.BaseEntity;
import com.gradge.erp.customer.entity.Customer;
import com.gradge.erp.tenant.entity.Tenant;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(
        name = "vehicles",
        indexes = {
                @Index(name = "idx_vehicle_tenant", columnList = "tenant_id"),
                @Index(name = "idx_vehicle_number", columnList = "vehicle_number")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vehicle extends BaseEntity {

    @Column(name = "vehicle_number", nullable = false)
    private String vehicleNumber;

    private String brand;
    private String model;
    private String color;

    @Column(name = "fuel_type")
    private String fuelType;

    @Column(name = "year_of_manufacture")
    private Integer yearOfManufacture;

    @Column(name = "chassis_number")
    private String chassisNumber;

    @Column(name = "engine_number")
    private String engineNumber;

    @Column(name = "last_service_date")
    private LocalDate lastServiceDate;

    @Column(name = "next_service_due")
    private LocalDate nextServiceDue;

    private Integer mileage;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "tenant_id",
            insertable = false,
            updatable = false
    )
    private Tenant tenant;

    @Column(name = "branch_id")
    private UUID branchId;
}