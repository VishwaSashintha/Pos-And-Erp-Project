package com.gradge.erp.finance.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.gradge.erp.common.entity.BaseEntity;
import com.gradge.erp.finance.enums.AccountType;
import com.gradge.erp.tenant.entity.Tenant;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(
        name = "accounts",
        uniqueConstraints = {
                @UniqueConstraint(name = "uc_account_code_tenant", columnNames = {"code", "tenant_id"})
        },
        indexes = {
                @Index(name = "idx_account_tenant", columnList = "tenant_id"),
                @Index(name = "idx_account_code", columnList = "code")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Account extends BaseEntity {

    @Column(nullable = false)
    private String code;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountType type;

    @Column(nullable = false)
    @Builder.Default
    private BigDecimal balance = BigDecimal.ZERO;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "tenant_id",
            nullable = false,
            insertable = false,
            updatable = false
    )
    private Tenant tenant;
}
