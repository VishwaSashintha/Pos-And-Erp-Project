package com.gradge.erp.auth.repository;

import com.gradge.erp.auth.entity.Employee;
import com.gradge.erp.auth.enums.EmployeeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, UUID> {
    List<Employee> findByStatus(EmployeeStatus status);
    List<Employee> findByTenantId(UUID tenantId);
    Optional<Employee> findByActivationToken(String activationToken);
}
