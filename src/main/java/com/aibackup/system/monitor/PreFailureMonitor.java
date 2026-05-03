package com.aibackup.system.monitor;

import com.aibackup.system.dto.DatabaseRequest;
import com.aibackup.system.service.BackupService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DriverManager;

@Component
public class PreFailureMonitor {

    @Value("${spring.datasource.url}")
    private String dbUrl;

    @Value("${spring.datasource.username}")
    private String dbUsername;

    @Value("${spring.datasource.password}")
    private String dbPassword;

    private final BackupService backupService;

    // 🔥 Track minor failures / risk
    private int warningCount = 0;
    private static final int WARNING_THRESHOLD = 1;

    public PreFailureMonitor(BackupService backupService) {
        this.backupService = backupService;
    }

    // ⏰ Run every 1 minute (adjust if needed)
    @Scheduled(fixedRate = 60000)
    public void monitorRisk() {

        long startTime = System.currentTimeMillis();

        try (Connection connection = DriverManager.getConnection(dbUrl, dbUsername, dbPassword)) {

            long responseTime = System.currentTimeMillis() - startTime;

            // 🐢 Slow DB = risk
            if (responseTime > 2000) {
                System.out.println("⚠️ Slow DB detected (" + responseTime + " ms)");
                triggerPreBackup();
            } else {
                System.out.println("✅ DB healthy (Response: " + responseTime + " ms)");
                warningCount = 0;
            }

        } catch (Exception e) {
            System.out.println("⚠️ Minor failure detected: " + e.getMessage());
            warningCount++;

            if (warningCount >= WARNING_THRESHOLD) {
                triggerPreBackup();
                warningCount = 0;
            }
        }
    }

    // 🔥 Pre-failure backup trigger
    private void triggerPreBackup() {
        try {
            System.out.println("🚀 Pre-failure backup triggered!");

            DatabaseRequest db = new DatabaseRequest();
            db.setUrl(dbUrl);
            db.setUsername(dbUsername);
            db.setPassword(dbPassword);

            backupService.takeBackup(db);

        } catch (Exception ex) {
            System.out.println("❌ Pre-backup failed: " + ex.getMessage());
        }
    }
}