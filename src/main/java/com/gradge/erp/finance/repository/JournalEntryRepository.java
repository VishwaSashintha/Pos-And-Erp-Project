package com.gradge.erp.finance.repository;

import com.gradge.erp.finance.entity.JournalEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface JournalEntryRepository extends JpaRepository<JournalEntry, UUID> {
    List<JournalEntry> findByTenantIdAndDeletedFalse(UUID tenantId);
    List<JournalEntry> findByTenantIdAndDeletedFalseAndEntryDateBetween(UUID tenantId, LocalDate start, LocalDate end);
    JournalEntry findByIdAndTenantId(UUID id, UUID tenantId);
}
