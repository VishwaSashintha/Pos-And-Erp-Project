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

    @Column(name = "subdomain", unique = true)
    private String subdomain;

    @Column(name = "custom_domain", unique = true)
    private String customDomain;

    @Column(name = "theme_color")
    private String themeColor;

    @Column(name = "logo_url")
    private String logoUrl;

    @Builder.Default
    @Column(name = "api_limit_per_minute", nullable = false)
    private int apiLimitPerMinute = 100;

    @Builder.Default
    @Column(name = "storage_quota_mb", nullable = false)
    private long storageQuotaMb = 1024; // 1 GB default
}
