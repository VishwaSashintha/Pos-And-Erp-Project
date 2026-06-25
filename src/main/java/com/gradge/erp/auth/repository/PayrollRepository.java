package com.gradge.erp.auth.repository;

import com.gradge.erp.auth.entity.PayrollRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PayrollRepository extends JpaRepository<PayrollRecord, UUID> {
    List<PayrollRecord> findByEmployee_IdAndTenantId(UUID employeeId, UUID tenantId);
    List<PayrollRecord> findByTenantId(UUID tenantId);
    boolean existsByEmployee_IdAndPayPeriodStartAndTenantId(UUID employeeId, java.time.LocalDate start, UUID tenantId);
}
