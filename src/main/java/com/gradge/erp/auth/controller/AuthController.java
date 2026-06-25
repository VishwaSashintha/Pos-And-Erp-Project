package com.gradge.erp.auth.controller;

import com.gradge.erp.auth.dto.*;
import com.gradge.erp.auth.entity.RefreshToken;
import com.gradge.erp.auth.entity.User;
import com.gradge.erp.auth.service.AuthService;
import com.gradge.erp.auth.service.RefreshTokenService;
import com.gradge.erp.common.response.ApiResponse;
import com.gradge.erp.security.config.JwtService;
import com.gradge.erp.tenant.service.TenantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final TenantService tenantService;
    private final RefreshTokenService refreshTokenService;
    private final JwtService jwtService;
    private final com.gradge.erp.security.config.JwtBlocklistService jwtBlocklistService;
    private final com.gradge.erp.auth.repository.EmployeeRepository employeeRepository;
    private final com.gradge.erp.auth.repository.UserRepository userRepository;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @PostMapping("/register-tenant")
    public ApiResponse<Map<String, Object>> registerTenant(@Valid @RequestBody TenantRegisterRequestDto request) {
        Map<String, Object> result = authService.registerTenantAndAdmin(
                request.getTenantName(),
                request.getUsername(),
                request.getPassword(),
                request.getEmail(),
                request.getIndustry(),
                request.getSelectedModules()
        );
        return ApiResponse.success("Tenant registered successfully", result);
    }

    @PostMapping("/register-user")
    @PreAuthorize("hasAuthority('MANAGE_USERS')")
    public ApiResponse<Map<String, Object>> registerUser(@Valid @RequestBody UserRegisterRequestDto request) {
        UUID tenantId = tenantService.getCurrentTenantId();
        User user = authService.registerUser(
                request.getUsername(),
                request.getPassword(),
                request.getEmail(),
                request.getRole(),
                tenantId
        );
        return ApiResponse.success("User registered successfully",
                Map.of("userId", user.getId(), "username", user.getUsername(), "role", user.getRole().name()));
    }

    @PostMapping("/login")
    public ApiResponse<Map<String, Object>> login(@Valid @RequestBody LoginRequestDto request) {
        Map<String, Object> result = authService.login(
                request.getUsername(),
                request.getPassword()
        );
        return ApiResponse.success("Login successful", result);
    }

    @PostMapping("/refresh")
    public ApiResponse<TokenRefreshResponseDto> refreshToken(@Valid @RequestBody RefreshTokenRequestDto request) {
        return refreshTokenService.findByToken(request.getRefreshToken())
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                    String accessToken = jwtService.generateToken(user.getUsername(), user.getRole().name(), user.getTenantId());
                    TokenRefreshResponseDto response = TokenRefreshResponseDto.builder()
                            .token(accessToken)
                            .refreshToken(request.getRefreshToken())
                            .build();
                    return ApiResponse.success("Token refreshed successfully", response);
                })
                .orElseThrow(() -> new RuntimeException("Refresh token is not in database"));
    }

    @PostMapping("/logout")
    public ApiResponse<String> logout(@RequestHeader("Authorization") String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            long ttlMillis = jwtService.extractExpiration(token).getTime() - System.currentTimeMillis();
            if (ttlMillis > 0) {
                jwtBlocklistService.addToBlocklist(token, ttlMillis);
            }
        }
        return ApiResponse.success("Logged out successfully");
    }

    @PostMapping("/onboard")
    public ApiResponse<Map<String, String>> onboardEmployee(@Valid @RequestBody OnboardRequestDto request) {
        com.gradge.erp.auth.entity.Employee employee = employeeRepository.findByActivationToken(request.getToken())
                .orElseThrow(() -> new RuntimeException("Invalid activation token"));

        if (employee.getActivationTokenExpiry().isBefore(java.time.LocalDateTime.now())) {
            throw new RuntimeException("Activation token has expired");
        }

        User user = userRepository.findByEmail(employee.getEmail())
                .orElseThrow(() -> new RuntimeException("User account not found for employee"));

        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setActive(true);
        userRepository.save(user);

        employee.setActivationToken(null);
        employee.setActivationTokenExpiry(null);
        employeeRepository.save(employee);

        return ApiResponse.success("Account activated successfully. You can now login.", 
                Map.of("username", user.getUsername()));
    }

    @PostMapping("/forgot-password")
    public ApiResponse<String> forgotPassword(@Valid @RequestBody ForgotPasswordRequestDto request) {
        authService.forgotPassword(request.getEmail());
        return ApiResponse.success("If the email exists, a password reset link has been sent.");
    }

    @PostMapping("/reset-password")
    public ApiResponse<String> resetPassword(@Valid @RequestBody ResetPasswordRequestDto request) {
        authService.resetPassword(request.getToken(), request.getNewPassword());
        return ApiResponse.success("Password has been reset successfully. You can now login.");
    }
}
