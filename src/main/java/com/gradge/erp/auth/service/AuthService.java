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
import java.time.LocalDateTime;
import jakarta.servlet.http.HttpServletRequest;

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
    private final com.gradge.erp.auth.repository.LoginHistoryRepository loginHistoryRepository;
    private final com.gradge.erp.security.mfa.TotpService totpService;
    private final HttpServletRequest request;
    private final LoginAttemptService loginAttemptService;

    @Transactional
    @Auditable(action = "TENANT_REGISTERED")
    public Map<String, Object> registerTenantAndAdmin(
            String tenantName,
            String username,
            String password,
            String email,
            String industry,
            String subdomain,
            String themeColor,
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
                .subdomain(subdomain)
                .themeColor(themeColor)
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

    @Transactional
    public Map<String, Object> login(String email, String password) {
        String ipAddress = request.getRemoteAddr();
        String userAgent = request.getHeader("User-Agent");
        String loginKey = email + "@" + ipAddress;

        if (loginAttemptService.isBlocked(loginKey)) {
            throw new RuntimeException("Account/IP is locked due to multiple failed login attempts. Try again later.");
        }

        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null) {
            loginAttemptService.loginFailed(loginKey);
            throw new RuntimeException("Invalid email or password");
        }

        if (!user.isActive()) {
            recordLoginHistory(user.getId(), ipAddress, userAgent, "FAILED_INACTIVE");
            throw new RuntimeException("User account is inactive");
        }

        if (user.getAccountLockedUntil() != null && user.getAccountLockedUntil().isAfter(LocalDateTime.now())) {
            recordLoginHistory(user.getId(), ipAddress, userAgent, "FAILED_LOCKED");
            throw new RuntimeException("Account is locked. Try again later.");
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            loginAttemptService.loginFailed(loginKey);
            user.setFailedLoginAttempts(user.getFailedLoginAttempts() + 1);
            if (user.getFailedLoginAttempts() >= 5) {
                user.setAccountLockedUntil(LocalDateTime.now().plusMinutes(15));
            }
            userRepository.save(user);
            recordLoginHistory(user.getId(), ipAddress, userAgent, "FAILED_BAD_CREDENTIALS");
            throw new RuntimeException("Invalid email or password");
        }

        // Reset failed attempts on success
        loginAttemptService.loginSucceeded(loginKey);
        user.setFailedLoginAttempts(0);
        user.setAccountLockedUntil(null);
        userRepository.save(user);

        if (user.isMfaEnabled()) {
            recordLoginHistory(user.getId(), ipAddress, userAgent, "MFA_REQUIRED");
            Map<String, Object> response = new HashMap<>();
            response.put("mfaRequired", true);
            response.put("email", user.getEmail());
            return response;
        }

        recordLoginHistory(user.getId(), ipAddress, userAgent, "SUCCESS");
        String token = jwtService.generateToken(user.getEmail(), user.getRole().name(), user.getTenantId());
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getEmail(), user.getTenantId());

        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("refreshToken", refreshToken.getToken());
        response.put("email", user.getEmail());
        response.put("username", user.getUsername());
        response.put("role", user.getRole().name());
        response.put("tenantId", user.getTenantId());
        return response;
    }

    private void recordLoginHistory(UUID userId, String ipAddress, String userAgent, String status) {
        com.gradge.erp.auth.entity.LoginHistory history = com.gradge.erp.auth.entity.LoginHistory.builder()
                .userId(userId)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .status(status)
                .build();
        // Since we may not have a tenantContext set for login, we bypass tenant requirement by setting it manually here if possible, or letting JPA handle it if we adapt BaseEntity.
        // Actually, user has a tenantId.
        history.setTenantId(userRepository.findById(userId).map(User::getTenantId).orElse(null));
        loginHistoryRepository.save(history);
    }

    public Map<String, Object> verifyMfa(String email, String code) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Invalid email"));
        
        if (!user.isMfaEnabled()) {
            throw new RuntimeException("MFA is not enabled for this user");
        }
        
        if (!totpService.verifyCode(user.getMfaSecret(), code)) {
            recordLoginHistory(user.getId(), request.getRemoteAddr(), request.getHeader("User-Agent"), "FAILED_MFA");
            throw new RuntimeException("Invalid MFA code");
        }
        
        recordLoginHistory(user.getId(), request.getRemoteAddr(), request.getHeader("User-Agent"), "SUCCESS_MFA");
        
        String token = jwtService.generateToken(user.getEmail(), user.getRole().name(), user.getTenantId());
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getEmail(), user.getTenantId());

        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("refreshToken", refreshToken.getToken());
        response.put("email", user.getEmail());
        response.put("username", user.getUsername());
        response.put("role", user.getRole().name());
        response.put("tenantId", user.getTenantId());
        return response;
    }

    public Map<String, String> setupMfa(String email) throws Exception {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Invalid email"));
        
        String secret = totpService.generateSecret();
        user.setMfaSecret(secret);
        // Don't enable yet, wait for them to verify it once
        userRepository.save(user);
        
        String qrCode = totpService.getQrCodeImageUri(secret, user.getEmail(), "Gradge ERP");
        
        Map<String, String> response = new HashMap<>();
        response.put("secret", secret);
        response.put("qrCode", qrCode);
        return response;
    }
    
    public void confirmMfaSetup(String email, String code) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Invalid email"));
                
        if (!totpService.verifyCode(user.getMfaSecret(), code)) {
            throw new RuntimeException("Invalid MFA code");
        }
        
        user.setMfaEnabled(true);
        userRepository.save(user);
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
