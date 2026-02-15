package com.aibackup.system.service;

import com.aibackup.system.entity.BackupLog;
import com.aibackup.system.repository.BackupLogRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;

@Service
public class BackupService {

    private final BackupLogRepository repository;

    public BackupService(BackupLogRepository repository) {
        this.repository = repository;
    }

    // ==============================
    // MANUAL BACKUP
    // ==============================
    public String takeBackup() {

        String backupDir = "C:/backup";

        // Auto create folder
        File directory = new File(backupDir);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        String fileName = "backup_" +
                LocalDateTime.now().toString().replace(":", "-") +
                ".backup";

        ProcessBuilder processBuilder = new ProcessBuilder(
                "C:\\Program Files\\PostgreSQL\\16\\bin\\pg_dump.exe",
                "-U", "postgres",
                "-F", "c",
                "-f", backupDir + "/" + fileName,
                "aibackup"
        );

        processBuilder.environment().put("PGPASSWORD", "Postgre@2202");

        try {

            Process process = processBuilder.start();
            int exitCode = process.waitFor();

            BackupLog log = new BackupLog();

            if (exitCode == 0) {
                log.setStatus("SUCCESS");
                log.setMessage("Backup Created: " + fileName);
                repository.save(log);
                return "Backup Created Successfully 🚀";
            } else {
                log.setStatus("FAILED");
                log.setMessage("Backup Process Error");
                repository.save(log);
                return "Backup Failed ❌";
            }

        } catch (IOException | InterruptedException e) {

            BackupLog log = new BackupLog();
            log.setStatus("FAILED");
            log.setMessage("Backup Exception Occurred");
            repository.save(log);

            return "Backup Failed ❌";
        }
    }

    // ==============================
    // AUTO BACKUP (Every 5 Days at 2 AM)
    // ==============================
    @Scheduled(cron = "0 0 2 */5 * ?")
    public void autoBackup() {
        takeBackup();
    }

    // ==============================
    // RESTORE BACKUP
    // ==============================
    public String restoreBackup(String fileName) {

        String backupDir = "C:/backup";
        String fullPath = backupDir + "/" + fileName;

        ProcessBuilder processBuilder = new ProcessBuilder(
                "C:\\Program Files\\PostgreSQL\\16\\bin\\pg_restore.exe",
                "-U", "postgres",
                "-d", "aibackup",
                "-c",
                fullPath
        );

        processBuilder.environment().put("PGPASSWORD", "Postgre@2202");

        try {

            Process process = processBuilder.start();
            int exitCode = process.waitFor();

            BackupLog log = new BackupLog();

            if (exitCode == 0) {
                log.setStatus("RESTORE_SUCCESS");
                log.setMessage("Database Restored: " + fileName);
                repository.save(log);
                return "Database Restored Successfully 🚀";
            } else {
                log.setStatus("RESTORE_FAILED");
                log.setMessage("Restore Process Error");
                repository.save(log);
                return "Restore Failed ❌";
            }

        } catch (Exception e) {

            BackupLog log = new BackupLog();
            log.setStatus("RESTORE_FAILED");
            log.setMessage("Restore Exception Occurred");
            repository.save(log);

            return "Restore Failed ❌";
        }
    }
}
