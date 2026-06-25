package com.gradge.erp.auth.controller;

import com.gradge.erp.auth.entity.Employee;
import com.gradge.erp.auth.entity.User;
import com.gradge.erp.auth.enums.EmployeeStatus;

import com.gradge.erp.auth.repository.EmployeeRepository;
import com.gradge.erp.auth.repository.UserRepository;
import com.gradge.erp.common.response.ApiResponse;
import com.gradge.erp.tenant.context.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import com.gradge.erp.common.audit.Auditable;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @PostMapping
    @PreAuthorize("hasAuthority('MANAGE_EMPLOYEES')")
    public ApiResponse<Employee> createEmployee(@RequestBody Employee request) {
        UUID tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new RuntimeException("No active tenant context");
        }

        Employee employee = Employee.builder()
                .name(request.getName())
                .email(request.getEmail())
                .role(request.getRole())
                .status(EmployeeStatus.PENDING_APPROVAL)
                .department(request.getDepartment())
                .build();
        employee.setTenantId(tenantId);
        
        Employee saved = employeeRepository.save(employee);
        return ApiResponse.success("Employee approval request submitted", saved);
    }

    @GetMapping("/{tenantId}")
    @PreAuthorize("hasAuthority('MANAGE_EMPLOYEES')")
    public ApiResponse<List<Employee>> getEmployees(@PathVariable("tenantId") UUID tenantId) {
        List<Employee> list = employeeRepository.findByTenantId(tenantId);
        return ApiResponse.success(list);
    }

    @GetMapping("/pending")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ApiResponse<List<Employee>> getPendingEmployees() {
        List<Employee> list = employeeRepository.findByStatus(EmployeeStatus.PENDING_APPROVAL);
        return ApiResponse.success(list);
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Auditable(action = "EMPLOYEE_APPROVED")
    public ApiResponse<Employee> approveEmployee(@PathVariable("id") UUID id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        if (employee.getStatus() != EmployeeStatus.PENDING_APPROVAL) {
            throw new RuntimeException("Employee is not pending approval");
        }

        // Generate Activation Token
        String token = UUID.randomUUID().toString();
        employee.setStatus(EmployeeStatus.APPROVED);
        employee.setActivationToken(token);
        employee.setActivationTokenExpiry(LocalDateTime.now().plusDays(7));
        Employee savedEmployee = employeeRepository.save(employee);

        // Generate User in inactive state
        // Derive username from email (e.g. part before @)
        String username = employee.getEmail().split("@")[0];
        
        // Ensure username is unique
        int counter = 1;
        String originalUsername = username;
        while (userRepository.findByUsername(username).isPresent()) {
            username = originalUsername + counter;
            counter++;
        }

        User user = User.builder()
                .username(username)
                .password(passwordEncoder.encode(UUID.randomUUID().toString())) // Random temporary password
                .email(employee.getEmail())
                .role(employee.getRole())
                .active(false) // Inactive until onboarding completes
                .build();
        user.setTenantId(employee.getTenantId());
        userRepository.save(user);

        System.out.println("-----------------------------------------------------------------");
        System.out.println("EMPLOYEE ONBOARDING GENERATED!");
        System.out.println("Employee Email: " + employee.getEmail());
        System.out.println("Activation URL: http://localhost:5173/onboard?token=" + token);
        System.out.println("-----------------------------------------------------------------");

        return ApiResponse.success("Employee approved, activation token generated", savedEmployee);
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ApiResponse<Employee> rejectEmployee(@PathVariable("id") UUID id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        if (employee.getStatus() != EmployeeStatus.PENDING_APPROVAL) {
            throw new RuntimeException("Employee is not pending approval");
        }

        employee.setStatus(EmployeeStatus.REJECTED);
        Employee saved = employeeRepository.save(employee);
        return ApiResponse.success("Employee request rejected", saved);
    }
}
