package com.gradge.erp.auth.repository;

import com.gradge.erp.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByUsernameAndTenantId(String username, UUID tenantId);
    Optional<User> findByUsername(String username);
    Optional<User> findByEmailAndTenantId(String email, UUID tenantId);
    Optional<User> findByEmail(String email);
    java.util.List<User> findByTenantId(UUID tenantId);
}
