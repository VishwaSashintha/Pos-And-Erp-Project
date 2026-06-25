package com.gradge.erp.workshop.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "inspection_items",
        indexes = {
                @Index(name = "idx_inspection_item_inspection", columnList = "inspection_id")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InspectionItem {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inspection_id", nullable = false)
    private Inspection inspection;

    @Column(name = "item_name", nullable = false)
    private String itemName;

    @Column(name = "is_checked")
    private boolean checked;

    @Column(columnDefinition = "TEXT")
    private String remarks;
}
