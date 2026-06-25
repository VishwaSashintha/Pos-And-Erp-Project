package com.gradge.erp.finance.repository;

import com.gradge.erp.finance.entity.Expense;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface ExpenseRepository extends JpaRepository<Expense, UUID> {
    List<Expense> findByTenantIdAndDeletedFalse(UUID tenantId);
    Expense findByIdAndTenantId(UUID id, UUID tenantId);
    List<Expense> findByTenantIdAndDeletedFalseAndDateBetween(UUID tenantId, LocalDate startDate, LocalDate endDate);
}
