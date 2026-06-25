package com.gradge.erp.auth.repository;

import com.gradge.erp.auth.entity.LeaveRequest;
import com.gradge.erp.auth.enums.LeaveStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, UUID> {
    List<LeaveRequest> findByTenantId(UUID tenantId);
    List<LeaveRequest> findByEmployee_IdAndTenantId(UUID employeeId, UUID tenantId);
    List<LeaveRequest> findByStatusAndTenantId(LeaveStatus status, UUID tenantId);
}
