package com.gradge.erp.auth.entity;

import com.gradge.erp.auth.enums.UserRole;
import com.gradge.erp.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "users",
        uniqueConstraints = {
                @UniqueConstraint(name = "uc_user_username_tenant", columnNames = {"username", "tenant_id"})
        },
        indexes = {
                @Index(name = "idx_user_tenant", columnList = "tenant_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseEntity {

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    @Builder.Default
    private boolean active = true;

    @Builder.Default
    @Column(name = "mfa_enabled", nullable = false)
    private boolean mfaEnabled = false;

    @Column(name = "mfa_secret")
    private String mfaSecret;

    @Builder.Default
    @Column(name = "failed_login_attempts", nullable = false)
    private int failedLoginAttempts = 0;

    @Column(name = "account_locked_until")
    private java.time.LocalDateTime accountLockedUntil;

    @Column(name = "password_changed_at")
    private java.time.LocalDateTime passwordChangedAt;
}
