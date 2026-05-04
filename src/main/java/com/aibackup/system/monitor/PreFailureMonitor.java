package com.aibackup.system.monitor;

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

    // 🔥 Track minor failures / risk
    private int warningCount = 0;
    private static final int WARNING_THRESHOLD = 2; // little safer

    // ⏰ Run every 1 minute
    @Scheduled(fixedRate = 60000)
    public void monitorRisk() {

        long startTime = System.currentTimeMillis();

        try (Connection connection = DriverManager.getConnection(dbUrl, dbUsername, dbPassword)) {

            long responseTime = System.currentTimeMillis() - startTime;

            // 🐢 Slow DB detection
            if (responseTime > 3000) {
                System.out.println("⚠️ Slow DB detected (" + responseTime + " ms)");
                System.out.println("🧠 AI ANALYSIS: Possible performance degradation (MEDIUM RISK ⚡)");
            } else {
                System.out.println("✅ DB healthy (Response: " + responseTime + " ms)");
                System.out.println("🧠 AI ANALYSIS: System Stable (LOW RISK ✅)");
                warningCount = 0;
            }

        } catch (Exception e) {
            System.out.println("⚠️ Connection issue: " + e.getMessage());
            warningCount++;

            if (warningCount >= WARNING_THRESHOLD) {
                System.out.println("🧠 AI ANALYSIS: Repeated failures detected (HIGH RISK ⚠️)");
                warningCount = 0;
            }
        }
    }
}