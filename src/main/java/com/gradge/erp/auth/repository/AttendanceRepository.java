package com.gradge.erp.auth.repository;

import com.gradge.erp.auth.entity.AttendanceRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AttendanceRepository extends JpaRepository<AttendanceRecord, UUID> {
    List<AttendanceRecord> findByEmployee_IdAndTenantId(UUID employeeId, UUID tenantId);
    List<AttendanceRecord> findByAttendanceDateAndTenantId(LocalDate date, UUID tenantId);
    Optional<AttendanceRecord> findByEmployee_IdAndAttendanceDateAndTenantId(UUID employeeId, LocalDate date, UUID tenantId);
    List<AttendanceRecord> findByEmployee_IdAndAttendanceDateBetweenAndTenantId(UUID employeeId, LocalDate start, LocalDate end, UUID tenantId);
}
