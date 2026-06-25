package com.gradge.erp.inventory.entity;

import com.gradge.erp.inventory.enums.StockMovementType;
import com.gradge.erp.tenant.entity.Tenant;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "stock_movements")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockMovement {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne
    private Product product;

    @Enumerated(EnumType.STRING)
    private StockMovementType type;

    private Double quantity;

    private String reference; 
    

    @ManyToOne
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    private LocalDateTime createdAt;
}
