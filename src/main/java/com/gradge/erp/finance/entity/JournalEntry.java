package com.gradge.erp.finance.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.gradge.erp.common.entity.BaseEntity;
import com.gradge.erp.tenant.entity.Tenant;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "journal_entries",
        indexes = {
                @Index(name = "idx_journal_tenant", columnList = "tenant_id"),
                @Index(name = "idx_journal_date", columnList = "entry_date")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JournalEntry extends BaseEntity {

    @Column(name = "entry_date", nullable = false)
    private LocalDate entryDate;

    @Column(nullable = false)
    private String description;

    @Column(name = "reference_number")
    private String referenceNumber;

    @Builder.Default
    @OneToMany(mappedBy = "journalEntry", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TransactionLine> lines = new ArrayList<>();

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "tenant_id",
            nullable = false,
            insertable = false,
            updatable = false
    )
    private Tenant tenant;

    public void addLine(TransactionLine line) {
        if (lines == null) {
            lines = new ArrayList<>();
        }
        lines.add(line);
        line.setJournalEntry(this);
    }
}
