package com.gradge.erp.workshop.entity;

import com.gradge.erp.common.entity.BaseEntity;
import com.gradge.erp.customer.entity.Customer;
import com.gradge.erp.pos.entity.Invoice;
import com.gradge.erp.tenant.entity.Tenant;
import com.gradge.erp.workshop.enums.JobCardStatus;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "job_cards")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobCard extends BaseEntity {

    @OneToOne(mappedBy = "jobCard")
    private Invoice invoice;

    @Column(unique = true, nullable = false, updatable = false)
    private String jobNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "tenant_id",
            insertable = false,
            updatable = false
    )
    private Tenant tenant;

    private String vehicleNumber;

    @Enumerated(EnumType.STRING)
    private JobCardStatus status;

    @Builder.Default
    private Double laborCost = 0.0;

    @Builder.Default
    private Double partsCost = 0.0;

    @Builder.Default
    private Double totalCost = 0.0;

    @Builder.Default
    private boolean invoiceGenerated = false;

    private UUID branchId;

    @Builder.Default
    private boolean deleted = false;
}