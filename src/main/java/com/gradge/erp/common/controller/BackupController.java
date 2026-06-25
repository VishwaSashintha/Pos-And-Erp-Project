package com.gradge.erp.common.controller;

import com.gradge.erp.common.response.ApiResponse;
import com.gradge.erp.common.service.DatabaseBackupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * REST controller for on-demand database backup operations.
 * Restricted to SUPER_ADMIN role only.
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/backup")
@RequiredArgsConstructor
public class BackupController {

    private final DatabaseBackupService backupService;

    @PostMapping("/trigger")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ApiResponse<Map<String, String>> triggerBackup() {
        try {
            String path = backupService.executeBackup();
            log.info("BACKUP: On-demand backup triggered successfully: {}", path);
            return ApiResponse.success("Backup completed successfully",
                    Map.of("backupFile", path));
        } catch (Exception e) {
            log.error("BACKUP: On-demand backup failed: {}", e.getMessage(), e);
            throw new RuntimeException("Backup failed: " + e.getMessage(), e);
        }
    }

    @PostMapping("/cleanup")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ApiResponse<String> triggerCleanup() {
        backupService.cleanupOldBackups();
        return ApiResponse.success("Old backup cleanup completed");
    }
}
