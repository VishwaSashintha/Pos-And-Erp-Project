package com.gradge.erp.pos.controller;

import com.gradge.erp.common.response.ApiResponse;
import com.gradge.erp.pos.entity.PosShift;
import com.gradge.erp.pos.service.PosShiftService;
import com.gradge.erp.tenant.context.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/pos/shifts")
@RequiredArgsConstructor
public class PosShiftController {

    private final PosShiftService posShiftService;

    @PostMapping("/open")
    @PreAuthorize("hasAuthority('MANAGE_POS')")
    public ApiResponse<PosShift> openShift(
            @RequestParam UUID userId,
            @RequestParam String terminalId,
            @RequestParam BigDecimal startingCash) {
        PosShift shift = posShiftService.openShift(userId, terminalId, startingCash, TenantContext.getTenantId());
        return ApiResponse.success("Shift opened", shift);
    }

    @PostMapping("/{shiftId}/close")
    @PreAuthorize("hasAuthority('MANAGE_POS')")
    public ApiResponse<PosShift> closeShift(
            @PathVariable UUID shiftId,
            @RequestParam BigDecimal actualCash,
            @RequestParam(required = false) String notes) {
        PosShift shift = posShiftService.closeShift(shiftId, actualCash, notes, TenantContext.getTenantId());
        return ApiResponse.success("Shift closed", shift);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('VIEW_POS_REPORTS')")
    public ApiResponse<List<PosShift>> getAll() {
        return ApiResponse.success("Shifts fetched", posShiftService.getAllShifts(TenantContext.getTenantId()));
    }
}
