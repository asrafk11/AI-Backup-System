package com.aibackup.system.monitor;

import com.aibackup.system.service.BackupService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDateTime;

@Component
public class SystemHealthMonitor {

    @Value("${spring.datasource.url}")
    private String dbUrl;

    @Value("${spring.datasource.username}")
    private String dbUsername;

    @Value("${spring.datasource.password}")
    private String dbPassword;

    private final BackupService backupService;

    // 🔥 Prevent false triggers
    private int failureCount = 0;
    private static final int FAILURE_THRESHOLD = 2;

    public SystemHealthMonitor(BackupService backupService) {
        this.backupService = backupService;
    }

    // ⏱ Runs every 30 sec
    @Scheduled(fixedRate = 30000)
    public void checkDatabaseHealth() {

        System.out.println("\n🔍 Checking DB at: " + LocalDateTime.now());
        System.out.println("DB: " + dbUrl);

        try (Connection connection = DriverManager.getConnection(dbUrl, dbUsername, dbPassword)) {

            if (connection != null && connection.isValid(2)) {
                System.out.println("✅ Database is UP");
                failureCount = 0;
            } else {
                System.out.println("⚠️ Connection invalid");
                handleFailure();
            }

        } catch (SQLException e) {
            System.out.println("❌ Database DOWN: " + e.getMessage());
            handleFailure();
        }
    }

    // 🚨 Handle failure safely
    private void handleFailure() {
        failureCount++;

        System.out.println("Failure Count: " + failureCount);

        if (failureCount >= FAILURE_THRESHOLD) {

            System.out.println("🚨 DB DOWN confirmed. Triggering backup...");

            try {
                backupService.triggerAutoBackup(dbUrl, dbUsername, dbPassword);
            } catch (Exception ex) {
                System.out.println("❌ Backup failed: " + ex.getMessage());
            }

            failureCount = 0; // reset
        }
    }
}