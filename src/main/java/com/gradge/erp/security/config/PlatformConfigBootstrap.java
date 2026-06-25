package com.gradge.erp.security.config;

import com.gradge.erp.auth.entity.User;
import com.gradge.erp.auth.enums.UserRole;
import com.gradge.erp.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PlatformConfigBootstrap implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.findByUsername("superadmin").isEmpty()) {
            User superAdmin = User.builder()
                    .username("superadmin")
                    .password(passwordEncoder.encode("superadmin123"))
                    .email("superadmin@platform.com")
                    .role(UserRole.SUPER_ADMIN)
                    .active(true)
                    .build();
            superAdmin.setTenantId(null); // Platform level, no tenant
            userRepository.save(superAdmin);
            System.out.println("Seeded default super admin user: superadmin / superadmin123");
        }
    }
}
