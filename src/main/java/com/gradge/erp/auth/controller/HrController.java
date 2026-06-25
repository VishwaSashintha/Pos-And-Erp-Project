package com.gradge.erp.auth.controller;

import com.gradge.erp.auth.entity.*;
import com.gradge.erp.auth.enums.LeaveType;
import com.gradge.erp.auth.service.HrService;
import com.gradge.erp.common.response.ApiResponse;
import com.gradge.erp.tenant.context.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/hr")
@RequiredArgsConstructor
public class HrController {

    private final HrService hrService;

    // ─── Leave ───────────────────────────────────────

    @PostMapping("/leaves")
    @PreAuthorize("hasAuthority('MANAGE_HR')")
    public ApiResponse<LeaveRequest> requestLeave(
            @RequestParam UUID employeeId,
            @RequestParam LeaveType leaveType,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String reason) {
        return ApiResponse.success("Leave request submitted",
                hrService.requestLeave(employeeId, leaveType, startDate, endDate, reason, TenantContext.getTenantId()));
    }

    @PostMapping("/leaves/{leaveId}/approve")
    @PreAuthorize("hasAuthority('MANAGE_HR')")
    public ApiResponse<LeaveRequest> approveLeave(@PathVariable UUID leaveId,
                                                    @RequestParam String approverId) {
        return ApiResponse.success("Leave approved",
                hrService.approveLeave(leaveId, approverId, TenantContext.getTenantId()));
    }

    @PostMapping("/leaves/{leaveId}/reject")
    @PreAuthorize("hasAuthority('MANAGE_HR')")
    public ApiResponse<LeaveRequest> rejectLeave(@PathVariable UUID leaveId,
                                                   @RequestParam String approverId,
                                                   @RequestParam String reason) {
        return ApiResponse.success("Leave rejected",
                hrService.rejectLeave(leaveId, approverId, reason, TenantContext.getTenantId()));
    }

    @GetMapping("/leaves/pending")
    @PreAuthorize("hasAuthority('MANAGE_HR')")
    public ApiResponse<List<LeaveRequest>> getPendingLeaves() {
        return ApiResponse.success("Pending leaves", hrService.getPendingLeaves(TenantContext.getTenantId()));
    }

    // ─── Attendance ─────────────────────────────────

    @PostMapping("/attendance/checkin")
    @PreAuthorize("hasAuthority('MANAGE_HR')")
    public ApiResponse<AttendanceRecord> checkIn(
            @RequestParam UUID employeeId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime time) {
        LocalDate d = date != null ? date : LocalDate.now();
        LocalTime t = time != null ? time : LocalTime.now();
        return ApiResponse.success("Checked in", hrService.checkIn(employeeId, d, t, TenantContext.getTenantId()));
    }

    @PostMapping("/attendance/checkout")
    @PreAuthorize("hasAuthority('MANAGE_HR')")
    public ApiResponse<AttendanceRecord> checkOut(
            @RequestParam UUID employeeId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime time) {
        LocalDate d = date != null ? date : LocalDate.now();
        LocalTime t = time != null ? time : LocalTime.now();
        return ApiResponse.success("Checked out", hrService.checkOut(employeeId, d, t, TenantContext.getTenantId()));
    }

    // ─── Payroll ─────────────────────────────────────

    @PostMapping("/payroll/generate")
    @PreAuthorize("hasAuthority('MANAGE_PAYROLL')")
    public ApiResponse<PayrollRecord> generatePayroll(
            @RequestParam UUID employeeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam BigDecimal basicSalary,
            @RequestParam(defaultValue = "0") BigDecimal allowances,
            @RequestParam(defaultValue = "0") BigDecimal deductions) {
        return ApiResponse.success("Payroll generated",
                hrService.generatePayroll(employeeId, startDate, endDate, basicSalary, allowances, deductions, TenantContext.getTenantId()));
    }

    @PostMapping("/payroll/{payrollId}/pay")
    @PreAuthorize("hasAuthority('MANAGE_PAYROLL')")
    public ApiResponse<PayrollRecord> processPayment(@PathVariable UUID payrollId) {
        return ApiResponse.success("Payment processed",
                hrService.processPayment(payrollId, TenantContext.getTenantId()));
    }
}
