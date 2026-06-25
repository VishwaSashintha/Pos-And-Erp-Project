package com.gradge.erp.common.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.stream.Stream;

/**
 * Automated database backup service using pg_dump.
 * Runs daily at 2 AM and enforces a 30-day retention policy.
 */
@Slf4j
@Service
public class DatabaseBackupService {

    @Value("${spring.datasource.url}")
    private String datasourceUrl;

    @Value("${spring.datasource.username}")
    private String dbUsername;

    @Value("${spring.datasource.password}")
    private String dbPassword;

    @Value("${backup.directory:./backups}")
    private String backupDirectory;

    @Value("${backup.retention-days:30}")
    private int retentionDays;

    /**
     * Scheduled backup job — runs daily at 2:00 AM.
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void performScheduledBackup() {
        log.info("BACKUP: Starting scheduled database backup at {}", LocalDateTime.now());
        try {
            String filePath = executeBackup();
            log.info("BACKUP: Completed successfully — {}", filePath);
            cleanupOldBackups();
        } catch (Exception e) {
            log.error("BACKUP: Failed — {}", e.getMessage(), e);
        }
    }

    /**
     * Execute a database backup using pg_dump.
     * @return Path to the generated backup file.
     */
    public String executeBackup() throws IOException, InterruptedException {
        // Ensure backup directory exists
        Path backupDir = Paths.get(backupDirectory);
        Files.createDirectories(backupDir);

        // Build filename with timestamp
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String filename = "lightbusiness_backup_" + timestamp + ".sql";
        Path backupFile = backupDir.resolve(filename);

        // Extract database name from JDBC URL (jdbc:postgresql://host:port/dbname)
        String dbName = extractDatabaseName(datasourceUrl);
        String dbHost = extractHost(datasourceUrl);
        String dbPort = extractPort(datasourceUrl);

        // Build pg_dump command
        ProcessBuilder processBuilder = new ProcessBuilder(
                "pg_dump",
                "-h", dbHost,
                "-p", dbPort,
                "-U", dbUsername,
                "-d", dbName,
                "-F", "c",  // custom format (compressed, supports pg_restore)
                "-f", backupFile.toAbsolutePath().toString()
        );

        // Set PGPASSWORD environment variable to avoid password prompt
        processBuilder.environment().put("PGPASSWORD", dbPassword);
        processBuilder.redirectErrorStream(true);

        Process process = processBuilder.start();
        int exitCode = process.waitFor();

        if (exitCode != 0) {
            String errorOutput = new String(process.getInputStream().readAllBytes());
            throw new RuntimeException("pg_dump failed with exit code " + exitCode + ": " + errorOutput);
        }

        log.info("BACKUP: Database backed up to {}", backupFile.toAbsolutePath());
        return backupFile.toAbsolutePath().toString();
    }

    /**
     * Clean up backup files older than the retention period.
     */
    public void cleanupOldBackups() {
        try {
            Path backupDir = Paths.get(backupDirectory);
            if (!Files.exists(backupDir)) return;

            LocalDate cutoff = LocalDate.now().minusDays(retentionDays);
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyyMMdd");

            try (Stream<Path> files = Files.list(backupDir)) {
                files.filter(f -> f.getFileName().toString().startsWith("lightbusiness_backup_"))
                     .filter(f -> {
                         try {
                             // Extract date portion from filename: lightbusiness_backup_YYYYMMDD_HHmmss.sql
                             String name = f.getFileName().toString();
                             String datePart = name.substring("lightbusiness_backup_".length(), "lightbusiness_backup_".length() + 8);
                             LocalDate fileDate = LocalDate.parse(datePart, fmt);
                             return fileDate.isBefore(cutoff);
                         } catch (Exception e) {
                             return false;
                         }
                     })
                     .forEach(f -> {
                         try {
                             Files.delete(f);
                             log.info("BACKUP: Cleaned up old backup — {}", f.getFileName());
                         } catch (IOException e) {
                             log.warn("BACKUP: Failed to delete old backup — {}", f.getFileName(), e);
                         }
                     });
            }
        } catch (IOException e) {
            log.error("BACKUP: Failed to list backup directory for cleanup — {}", e.getMessage(), e);
        }
    }

    private String extractDatabaseName(String url) {
        // jdbc:postgresql://host:port/dbname?params
        String withoutPrefix = url.replace("jdbc:postgresql://", "");
        String afterSlash = withoutPrefix.contains("/") ? withoutPrefix.substring(withoutPrefix.indexOf("/") + 1) : "erp_db";
        return afterSlash.contains("?") ? afterSlash.substring(0, afterSlash.indexOf("?")) : afterSlash;
    }

    private String extractHost(String url) {
        String withoutPrefix = url.replace("jdbc:postgresql://", "");
        String hostPort = withoutPrefix.contains("/") ? withoutPrefix.substring(0, withoutPrefix.indexOf("/")) : withoutPrefix;
        return hostPort.contains(":") ? hostPort.substring(0, hostPort.indexOf(":")) : hostPort;
    }

    private String extractPort(String url) {
        String withoutPrefix = url.replace("jdbc:postgresql://", "");
        String hostPort = withoutPrefix.contains("/") ? withoutPrefix.substring(0, withoutPrefix.indexOf("/")) : withoutPrefix;
        return hostPort.contains(":") ? hostPort.substring(hostPort.indexOf(":") + 1) : "5432";
    }
}
