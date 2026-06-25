package com.gradge.erp.tenant.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

import com.gradge.erp.billing.model.AppModule;
import java.util.Set;
import java.util.HashSet;

@Entity
@Table(name = "tenants")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tenant {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(name = "industry")
    private String industry;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "tenant_modules", joinColumns = @JoinColumn(name = "tenant_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "module_key")
    @Builder.Default
    private Set<AppModule> enabledModules = new HashSet<>();
}
