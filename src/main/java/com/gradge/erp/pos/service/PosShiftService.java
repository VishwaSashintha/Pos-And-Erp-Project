package com.gradge.erp.pos.service;

import com.gradge.erp.auth.entity.User;
import com.gradge.erp.auth.repository.UserRepository;
import com.gradge.erp.pos.entity.PosShift;
import com.gradge.erp.pos.enums.ShiftStatus;
import com.gradge.erp.pos.repository.PosShiftRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PosShiftService {

    private final PosShiftRepository posShiftRepository;
    private final UserRepository userRepository;

    @Transactional
    public PosShift openShift(UUID userId, String terminalId, BigDecimal startingCash, UUID tenantId) {
        // Ensure no open shift for this terminal already
        posShiftRepository.findByPosTerminalIdAndStatusAndTenantId(terminalId, ShiftStatus.OPEN, tenantId)
                .ifPresent(s -> { throw new RuntimeException("A shift is already open on terminal: " + terminalId); });

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        PosShift shift = PosShift.builder()
                .user(user)
                .posTerminalId(terminalId)
                .openedAt(LocalDateTime.now())
                .status(ShiftStatus.OPEN)
                .startingCash(startingCash)
                .expectedCash(startingCash)
                .actualCash(BigDecimal.ZERO)
                .build();
        shift.setTenantId(tenantId);

        log.info("Shift opened by user {} on terminal {} at {}", userId, terminalId, shift.getOpenedAt());
        return posShiftRepository.save(shift);
    }

    @Transactional
    public PosShift closeShift(UUID shiftId, BigDecimal actualCash, String notes, UUID tenantId) {
        PosShift shift = posShiftRepository.findById(shiftId)
                .orElseThrow(() -> new RuntimeException("Shift not found"));

        if (!shift.getTenantId().equals(tenantId)) {
            throw new RuntimeException("Unauthorized access to shift");
        }
        if (shift.getStatus() != ShiftStatus.OPEN) {
            throw new RuntimeException("Shift is already closed");
        }

        shift.setStatus(ShiftStatus.CLOSED);
        shift.setClosedAt(LocalDateTime.now());
        shift.setActualCash(actualCash);
        shift.setClosingNotes(notes);

        BigDecimal variance = actualCash.subtract(shift.getExpectedCash());
        log.info("Shift {} closed. Expected: {}, Actual: {}, Variance: {}", shiftId, shift.getExpectedCash(), actualCash, variance);

        return posShiftRepository.save(shift);
    }

    @Transactional
    public void addCashToShift(UUID shiftId, BigDecimal amount, UUID tenantId) {
        PosShift shift = getActiveShift(shiftId, tenantId);
        shift.setExpectedCash(shift.getExpectedCash().add(amount));
        posShiftRepository.save(shift);
    }

    public PosShift getActiveShift(UUID shiftId, UUID tenantId) {
        PosShift shift = posShiftRepository.findById(shiftId)
                .orElseThrow(() -> new RuntimeException("Shift not found"));
        if (!shift.getTenantId().equals(tenantId)) {
            throw new RuntimeException("Unauthorized access to shift");
        }
        if (shift.getStatus() != ShiftStatus.OPEN) {
            throw new RuntimeException("Shift is not open");
        }
        return shift;
    }

    public List<PosShift> getAllShifts(UUID tenantId) {
        return posShiftRepository.findByTenantId(tenantId);
    }
}
