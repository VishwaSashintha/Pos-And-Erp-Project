package com.gradge.erp.workshop.repository;

import com.gradge.erp.workshop.entity.JobCard;
import com.gradge.erp.workshop.enums.JobCardStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface JobCardRepository extends JpaRepository<JobCard, UUID> {

    List<JobCard> findByTenant_IdAndDeletedFalse(UUID tenantId);

    List<JobCard> findByStatusAndTenant_Id(JobCardStatus status, UUID tenantId);

    JobCard findByIdAndTenant_Id(UUID id, UUID tenantId);

    JobCard findByJobNumberAndTenant_Id(String jobNumber, UUID tenantId);
}
