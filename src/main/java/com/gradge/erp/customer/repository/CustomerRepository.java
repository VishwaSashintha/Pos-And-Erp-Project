package com.gradge.erp.customer.repository;

import com.gradge.erp.customer.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CustomerRepository extends JpaRepository<Customer, UUID> {

    List<Customer> findByTenant_IdAndDeletedFalse(UUID tenantId);

    Customer findByIdAndTenant_Id(UUID id, UUID tenantId);

    Customer findByPhoneAndTenant_Id(String phone, UUID tenantId);
}
