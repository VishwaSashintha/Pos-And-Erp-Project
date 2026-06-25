package com.gradge.erp.auth.entity;

import com.gradge.erp.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "payroll_records", indexes = {
        @Index(name = "idx_payroll_employee_period", columnList = "employee_id, pay_period_start")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PayrollRecord extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(name = "pay_period_start", nullable = false)
    private LocalDate payPeriodStart;

    @Column(name = "pay_period_end", nullable = false)
    private LocalDate payPeriodEnd;

    @Column(nullable = false)
    private BigDecimal basicSalary;

    @Builder.Default
    private BigDecimal allowances = BigDecimal.ZERO;

    @Builder.Default
    private BigDecimal deductions = BigDecimal.ZERO;

    @Builder.Default
    private BigDecimal overtimePay = BigDecimal.ZERO;

    @Builder.Default
    private BigDecimal netPay = BigDecimal.ZERO;

    @Column(name = "days_worked")
    private Integer daysWorked;

    @Column(name = "days_absent")
    private Integer daysAbsent;

    @Column(name = "is_paid")
    @Builder.Default
    private boolean paid = false;

    @Column(name = "payment_date")
    private LocalDate paymentDate;

    private String notes;

    public BigDecimal calculateNetPay() {
        return basicSalary.add(allowances).add(overtimePay).subtract(deductions);
    }
}
