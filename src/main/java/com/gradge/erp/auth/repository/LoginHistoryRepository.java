package com.gradge.erp.auth.repository;

import com.gradge.erp.auth.entity.LoginHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;
import java.util.List;

@Repository
public interface LoginHistoryRepository extends JpaRepository<LoginHistory, UUID> {
    List<LoginHistory> findByUserIdOrderByCreatedAtDesc(UUID userId);
}
