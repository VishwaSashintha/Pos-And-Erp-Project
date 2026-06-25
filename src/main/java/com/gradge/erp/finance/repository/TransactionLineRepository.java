package com.gradge.erp.finance.repository;

import com.gradge.erp.finance.entity.TransactionLine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TransactionLineRepository extends JpaRepository<TransactionLine, UUID> {
    List<TransactionLine> findByTenantIdAndDeletedFalse(UUID tenantId);
}
