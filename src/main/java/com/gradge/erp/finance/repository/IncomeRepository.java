package com.gradge.erp.finance.repository;

import com.gradge.erp.finance.entity.Income;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface IncomeRepository extends JpaRepository<Income, UUID> {
    List<Income> findByTenantIdAndDeletedFalse(UUID tenantId);
    Income findByIdAndTenantId(UUID id, UUID tenantId);
    List<Income> findByTenantIdAndDeletedFalseAndDateBetween(UUID tenantId, LocalDate startDate, LocalDate endDate);
}
