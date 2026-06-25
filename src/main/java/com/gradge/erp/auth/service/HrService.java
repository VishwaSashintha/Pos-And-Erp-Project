package com.gradge.erp.auth.service;

import com.gradge.erp.auth.entity.*;
import com.gradge.erp.auth.enums.LeaveStatus;
import com.gradge.erp.auth.enums.LeaveType;
import com.gradge.erp.auth.repository.*;
import com.gradge.erp.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class HrService {

    private final EmployeeRepository employeeRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final AttendanceRepository attendanceRepository;
    private final PayrollRepository payrollRepository;
    private final NotificationService notificationService;

    // ─────────────────────────────────────────────
    // Leave Management
    // ─────────────────────────────────────────────

    @Transactional
    public LeaveRequest requestLeave(UUID employeeId, LeaveType type, LocalDate start, LocalDate end, String reason, UUID tenantId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        if (!employee.getTenantId().equals(tenantId)) {
            throw new RuntimeException("Unauthorized access to employee");
        }

        if (end.isBefore(start)) {
            throw new RuntimeException("End date cannot be before start date");
        }

        LeaveRequest request = LeaveRequest.builder()
                .employee(employee)
                .leaveType(type)
                .startDate(start)
                .endDate(end)
                .reason(reason)
                .status(LeaveStatus.PENDING)
                .build();
        request.setTenantId(tenantId);

        return leaveRequestRepository.save(request);
    }

    @Transactional
    public LeaveRequest approveLeave(UUID leaveId, String approverId, UUID tenantId) {
        LeaveRequest request = getLeaveRequest(leaveId, tenantId);
        request.setStatus(LeaveStatus.APPROVED);
        request.setApprovedByUserId(approverId);

        notificationService.sendEmail(
                request.getEmployee().getEmail(),
                "Leave Request Approved",
                "Dear " + request.getEmployee().getName() + ", your " + request.getLeaveType().name() +
                        " leave from " + request.getStartDate() + " to " + request.getEndDate() + " has been approved."
        );
        return leaveRequestRepository.save(request);
    }

    @Transactional
    public LeaveRequest rejectLeave(UUID leaveId, String approverId, String rejectionReason, UUID tenantId) {
        LeaveRequest request = getLeaveRequest(leaveId, tenantId);
        request.setStatus(LeaveStatus.REJECTED);
        request.setApprovedByUserId(approverId);
        request.setRejectionReason(rejectionReason);

        notificationService.sendEmail(
                request.getEmployee().getEmail(),
                "Leave Request Rejected",
                "Dear " + request.getEmployee().getName() + ", your leave request has been rejected. Reason: " + rejectionReason
        );
        return leaveRequestRepository.save(request);
    }

    public List<LeaveRequest> getPendingLeaves(UUID tenantId) {
        return leaveRequestRepository.findByStatusAndTenantId(LeaveStatus.PENDING, tenantId);
    }

    // ─────────────────────────────────────────────
    // Attendance
    // ─────────────────────────────────────────────

    @Transactional
    public AttendanceRecord checkIn(UUID employeeId, LocalDate date, LocalTime checkInTime, UUID tenantId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        attendanceRepository.findByEmployee_IdAndAttendanceDateAndTenantId(employeeId, date, tenantId)
                .ifPresent(r -> { throw new RuntimeException("Attendance already recorded for this date"); });

        LocalTime standardStart = LocalTime.of(8, 30);
        AttendanceRecord record = AttendanceRecord.builder()
                .employee(employee)
                .attendanceDate(date)
                .checkInTime(checkInTime)
                .present(true)
                .late(checkInTime.isAfter(standardStart))
                .build();
        record.setTenantId(tenantId);
        return attendanceRepository.save(record);
    }

    @Transactional
    public AttendanceRecord checkOut(UUID employeeId, LocalDate date, LocalTime checkOutTime, UUID tenantId) {
        AttendanceRecord record = attendanceRepository
                .findByEmployee_IdAndAttendanceDateAndTenantId(employeeId, date, tenantId)
                .orElseThrow(() -> new RuntimeException("No check-in found for today"));

        record.setCheckOutTime(checkOutTime);
        if (record.getCheckInTime() != null) {
            double hours = record.getCheckInTime().until(checkOutTime, ChronoUnit.MINUTES) / 60.0;
            record.setTotalHours(Math.max(hours, 0));
        }
        return attendanceRepository.save(record);
    }

    // ─────────────────────────────────────────────
    // Payroll
    // ─────────────────────────────────────────────

    @Transactional
    public PayrollRecord generatePayroll(UUID employeeId, LocalDate start, LocalDate end,
                                          BigDecimal basicSalary, BigDecimal allowances,
                                          BigDecimal deductions, UUID tenantId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        if (payrollRepository.existsByEmployee_IdAndPayPeriodStartAndTenantId(employeeId, start, tenantId)) {
            throw new RuntimeException("Payroll already generated for this period");
        }

        // Count attendance
        List<AttendanceRecord> attendance = attendanceRepository
                .findByEmployee_IdAndAttendanceDateBetweenAndTenantId(employeeId, start, end, tenantId);
        long daysPresent = attendance.stream().filter(AttendanceRecord::isPresent).count();

        PayrollRecord record = PayrollRecord.builder()
                .employee(employee)
                .payPeriodStart(start)
                .payPeriodEnd(end)
                .basicSalary(basicSalary)
                .allowances(allowances)
                .deductions(deductions)
                .overtimePay(BigDecimal.ZERO)
                .daysWorked((int) daysPresent)
                .build();
        record.setTenantId(tenantId);
        record.setNetPay(record.calculateNetPay());
        return payrollRepository.save(record);
    }

    @Transactional
    public PayrollRecord processPayment(UUID payrollId, UUID tenantId) {
        PayrollRecord record = payrollRepository.findById(payrollId)
                .orElseThrow(() -> new RuntimeException("Payroll record not found"));

        if (!record.getTenantId().equals(tenantId)) {
            throw new RuntimeException("Unauthorized access to payroll record");
        }
        if (record.isPaid()) {
            throw new RuntimeException("Payroll already paid");
        }

        record.setPaid(true);
        record.setPaymentDate(LocalDate.now());
        PayrollRecord saved = payrollRepository.save(record);

        notificationService.sendEmail(
                record.getEmployee().getEmail(),
                "Payslip - " + record.getPayPeriodStart() + " to " + record.getPayPeriodEnd(),
                "Dear " + record.getEmployee().getName() + ",\n\nYour net pay of " + record.getNetPay() +
                        " has been processed.\n\nBasic: " + record.getBasicSalary() +
                        "\nAllowances: " + record.getAllowances() +
                        "\nDeductions: " + record.getDeductions() +
                        "\n\nThank you."
        );
        return saved;
    }

    private LeaveRequest getLeaveRequest(UUID leaveId, UUID tenantId) {
        LeaveRequest request = leaveRequestRepository.findById(leaveId)
                .orElseThrow(() -> new RuntimeException("Leave request not found"));
        if (!request.getTenantId().equals(tenantId)) {
            throw new RuntimeException("Unauthorized access to leave request");
        }
        return request;
    }
}
