package com.gradge.erp.pos.entity;

import com.gradge.erp.common.entity.BaseEntity;
import com.gradge.erp.customer.entity.Customer;
import com.gradge.erp.pos.enums.InvoiceStatus;
import com.gradge.erp.tenant.entity.Tenant;
import com.gradge.erp.workshop.entity.JobCard;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "invoices")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Invoice extends BaseEntity {

    private String invoiceNumber;

    @ManyToOne
    private Customer customer;

    @OneToOne
    private JobCard jobCard;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pos_shift_id")
    private PosShift posShift;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "tenant_id",
            insertable = false,
            updatable = false
    )
    private Tenant tenant;

    @Builder.Default
    private BigDecimal subTotal = BigDecimal.ZERO;

    @Builder.Default
    private BigDecimal discount = BigDecimal.ZERO;

    @Column(name = "discount_type")
    @Builder.Default
    private String discountType = "FIXED"; // FIXED or PERCENTAGE

    @Builder.Default
    private String notes = "";

    @Builder.Default
    private BigDecimal tax = BigDecimal.ZERO;

    @Builder.Default
    private BigDecimal total = BigDecimal.ZERO;

    @Builder.Default
    private BigDecimal balance = BigDecimal.ZERO;

    @Column(nullable = false)
    @Builder.Default
    private BigDecimal paidAmount = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    private InvoiceStatus status;

    @OneToMany(
            mappedBy = "invoice",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @Builder.Default
    private List<InvoiceItem> items = new ArrayList<>();

    public void addItem(InvoiceItem item) {
        items.add(item);
        item.setInvoice(this);
    }

    public BigDecimal getBalance() {
        return total.subtract(paidAmount);
    }
}