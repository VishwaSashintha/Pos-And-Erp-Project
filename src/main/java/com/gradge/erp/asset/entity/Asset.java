package com.gradge.erp.asset.entity;

import com.gradge.erp.common.entity.BaseEntity;
import com.gradge.erp.auth.entity.Employee;
import com.gradge.erp.asset.enums.AssetStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "assets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Asset extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String serialNumber;

    @Column(nullable = false)
    private String category; // e.g. IT, VEHICLE, FURNITURE

    private Double purchasePrice;

    private LocalDate purchaseDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AssetStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to_id")
    private Employee assignedTo;

    private String location;
    private String notes;
}
