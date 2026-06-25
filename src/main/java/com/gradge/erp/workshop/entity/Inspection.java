package com.gradge.erp.workshop.entity;

import com.gradge.erp.tenant.entity.Tenant;
import com.gradge.erp.workshop.enums.InspectionStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "inspections",
        indexes = {
                @Index(name = "idx_inspection_tenant", columnList = "tenant_id"),
                @Index(name = "idx_inspection_jobcard", columnList = "job_card_id")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Inspection {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_card_id", nullable = false)
    private JobCard jobCard;

    @Enumerated(EnumType.STRING)
    private InspectionStatus status;

    @Column(columnDefinition = "TEXT")
    private String overallNotes;

    @Column(name = "estimated_cost")
    private Double estimatedCost;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @Column(name = "branch_id")
    private UUID branchId;

    @Column(name = "is_deleted")
    @Builder.Default
    private boolean deleted = false;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
