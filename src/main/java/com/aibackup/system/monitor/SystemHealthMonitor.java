package com.aibackup.system.monitor;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.sql.Connection;
import java.sql.DriverManager;

@Component
public class SystemHealthMonitor {

    @Scheduled(fixedRate = 30000)
    public void checkDatabaseHealth() {
        try {
            Connection con = DriverManager.getConnection(
                    "jdbc:mysql://localhost:5432/aibackup",
                    "postgres",
                    "Postgre@2202"
            );

            if (con.isValid(2)) {
                System.out.println("Database is healthy ✅");
            }

        } catch (Exception e) {
            System.out.println("Database connection failed ⚠️");
            // Here you could trigger backup or alert
        }
    }
}