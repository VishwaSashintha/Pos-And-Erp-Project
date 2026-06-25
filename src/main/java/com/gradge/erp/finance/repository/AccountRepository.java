package com.gradge.erp.finance.repository;

import com.gradge.erp.finance.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccountRepository extends JpaRepository<Account, UUID> {
    List<Account> findByTenantIdAndDeletedFalse(UUID tenantId);
    Optional<Account> findByCodeAndTenantIdAndDeletedFalse(String code, UUID tenantId);
    Account findByIdAndTenantId(UUID id, UUID tenantId);
}
