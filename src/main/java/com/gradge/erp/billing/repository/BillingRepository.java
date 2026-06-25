package com.gradge.erp.billing.repository;

import com.gradge.erp.billing.entity.BillingRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface BillingRepository extends JpaRepository<BillingRecord, UUID> {

    List<BillingRecord> findByTenant_Id(UUID tenantId);
}
