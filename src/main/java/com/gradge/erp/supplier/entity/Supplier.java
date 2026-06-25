package com.gradge.erp.supplier.entity;

import com.gradge.erp.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "suppliers",
        indexes = {
                @Index(name = "idx_supplier_tenant", columnList = "tenant_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Supplier extends BaseEntity {

    @Column(nullable = false)
    private String name;

    private String contactName;

    private String phone;

    private String email;

    private String address;

    @Column(name = "tax_number")
    private String taxNumber;
}
