package com.gradge.erp.workshop.repository;

import com.gradge.erp.workshop.entity.Inspection;
import com.gradge.erp.workshop.enums.InspectionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface InspectionRepository extends JpaRepository<Inspection, UUID> {

    List<Inspection> findByTenant_IdAndDeletedFalse(UUID tenantId);

    List<Inspection> findByJobCard_IdAndTenant_Id(UUID jobCardId, UUID tenantId);

    Inspection findByIdAndTenant_Id(UUID id, UUID tenantId);

    List<Inspection> findByStatusAndTenant_Id(InspectionStatus status, UUID tenantId);
}
