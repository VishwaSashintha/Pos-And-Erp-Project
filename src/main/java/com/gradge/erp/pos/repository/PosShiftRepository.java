package com.gradge.erp.pos.repository;

import com.gradge.erp.pos.entity.PosShift;
import com.gradge.erp.pos.enums.ShiftStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PosShiftRepository extends JpaRepository<PosShift, UUID> {
    List<PosShift> findByTenantId(UUID tenantId);
    Optional<PosShift> findByPosTerminalIdAndStatusAndTenantId(String terminalId, ShiftStatus status, UUID tenantId);
    Optional<PosShift> findByUser_IdAndStatusAndTenantId(UUID userId, ShiftStatus status, UUID tenantId);
}
