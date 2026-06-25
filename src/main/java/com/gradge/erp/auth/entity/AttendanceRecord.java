package com.gradge.erp.auth.entity;

import com.gradge.erp.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "attendance_records", indexes = {
        @Index(name = "idx_attendance_employee_date", columnList = "employee_id, attendance_date")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceRecord extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(name = "attendance_date", nullable = false)
    private LocalDate attendanceDate;

    @Column(name = "check_in_time")
    private LocalTime checkInTime;

    @Column(name = "check_out_time")
    private LocalTime checkOutTime;

    @Column(name = "total_hours")
    private Double totalHours;

    @Column(name = "is_present")
    @Builder.Default
    private boolean present = false;

    @Column(name = "is_late")
    @Builder.Default
    private boolean late = false;

    private String notes;
}
