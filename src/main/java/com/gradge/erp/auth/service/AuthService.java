package com.gradge.erp.auth.service;

import com.gradge.erp.auth.entity.RefreshToken;
import com.gradge.erp.auth.entity.User;
import com.gradge.erp.auth.enums.UserRole;
import com.gradge.erp.auth.repository.UserRepository;
import com.gradge.erp.security.config.JwtService;
import com.gradge.erp.tenant.entity.Tenant;
import com.gradge.erp.tenant.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.gradge.erp.finance.service.LedgerService;
import com.gradge.erp.notification.service.NotificationService;
import com.gradge.erp.billing.entity.TenantSubscription;
import com.gradge.erp.billing.model.SubscriptionPlanType;
import com.gradge.erp.billing.repository.TenantSubscriptionRepository;
import com.gradge.erp.common.audit.Auditable;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final LedgerService ledgerService;
    private final NotificationService notificationService;
    private final TenantSubscriptionRepository tenantSubscriptionRepository;
    private final RefreshTokenService refreshTokenService;
    private final com.gradge.erp.auth.repository.PasswordResetTokenRepository passwordResetTokenRepository;

    @Transactional
    @Auditable(action = "TENANT_REGISTERED")
    public Map<String, Object> registerTenantAndAdmin(
            String tenantName,
            String username,
            String password,
            String email,
            String industry,
            java.util.List<com.gradge.erp.billing.model.AppModule> selectedModules
    ) {
        if (tenantRepository.findByName(tenantName).isPresent()) {
            throw new RuntimeException("Tenant name already exists");
        }
        if (userRepository.findByUsername(username).isPresent()) {
            throw new RuntimeException("Username already exists");
        }

        Tenant tenant = Tenant.builder()
                .name(tenantName)
                .industry(industry)
                .enabledModules(selectedModules != null ? new java.util.HashSet<>(selectedModules) : new java.util.HashSet<>())
                .build();
        tenant = tenantRepository.save(tenant);

        TenantSubscription subscription = TenantSubscription.builder()
                .tenant(tenant)
                .planType(SubscriptionPlanType.ENTERPRISE)
                .startDate(java.time.LocalDate.now())
                .endDate(java.time.LocalDate.now().plusYears(1))
                .active(true)
                .build();
        tenantSubscriptionRepository.save(subscription);

        ledgerService.initializeDefaultAccounts(tenant.getId());

        User user = User.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .email(email)
                .role(UserRole.ADMIN)
                .active(true)
                .build();
        user.setTenantId(tenant.getId());
        userRepository.save(user);

        if (email != null && !email.isBlank()) {
            notificationService.sendEmail(
                email,
                "Welcome to Gradge ERP!",
                "Hello " + username + ",\n\nYour ERP tenant '" + tenantName + "' and admin account have been successfully registered.\n\nBest regards,\nGradge ERP Team"
            );
        }

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Tenant and Admin registered successfully");
        response.put("tenantId", tenant.getId());
        response.put("username", username);
        return response;
    }

    @Transactional
    public User registerUser(
            String username,
            String password,
            String email,
            UserRole role,
            UUID tenantId
    ) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new RuntimeException("Tenant not found"));

        if (userRepository.findByUsernameAndTenantId(username, tenantId).isPresent()) {
            throw new RuntimeException("Username already exists in this tenant");
        }

        User user = User.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .email(email)
                .role(role)
                .active(true)
                .build();
        user.setTenantId(tenant.getId());
        
        User savedUser = userRepository.save(user);

        if (email != null && !email.isBlank()) {
            notificationService.sendEmail(
                email,
                "Gradge ERP User Activation",
                "Hello " + username + ",\n\nYour user account has been registered with role: " + role.name() + " for tenant: " + tenant.getName() + ".\n\nBest regards,\nGradge ERP Team"
            );
        }

        return savedUser;
    }

    public Map<String, Object> login(String username, String password) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Invalid username or password"));

        if (!user.isActive()) {
            throw new RuntimeException("User account is inactive");
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid username or password");
        }

        String token = jwtService.generateToken(user.getUsername(), user.getRole().name(), user.getTenantId());
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getUsername(), user.getTenantId());

        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("refreshToken", refreshToken.getToken());
        response.put("username", user.getUsername());
        response.put("role", user.getRole().name());
        response.put("tenantId", user.getTenantId());
        return response;
    }

    @Transactional
    public void forgotPassword(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

        // Delete any existing tokens for this user
        passwordResetTokenRepository.deleteByUserId(user.getId());

        String token = UUID.randomUUID().toString();
        com.gradge.erp.auth.entity.PasswordResetToken resetToken = com.gradge.erp.auth.entity.PasswordResetToken.builder()
                .user(user)
                .token(token)
                .expiryDate(java.time.Instant.now().plusSeconds(3600)) // 1 hour validity
                .tenantId(user.getTenantId())
                .build();

        passwordResetTokenRepository.save(resetToken);

        if (user.getEmail() != null && !user.getEmail().isBlank()) {
            notificationService.sendEmail(
                user.getEmail(),
                "Password Reset Request - Gradge ERP",
                "Hello " + user.getUsername() + ",\n\n" +
                "You have requested to reset your password. Please use the following token to reset it:\n\n" +
                token + "\n\n" +
                "This token is valid for 1 hour.\n\nBest regards,\nGradge ERP Team"
            );
        }
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        com.gradge.erp.auth.entity.PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid password reset token"));

        if (resetToken.getExpiryDate().isBefore(java.time.Instant.now())) {
            passwordResetTokenRepository.delete(resetToken);
            throw new RuntimeException("Password reset token has expired");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Invalidate token after use
        passwordResetTokenRepository.delete(resetToken);
    }
}
