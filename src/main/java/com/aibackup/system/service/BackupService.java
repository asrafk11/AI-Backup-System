package com.aibackup.system.service;

import com.aibackup.system.entity.BackupLog;
import com.aibackup.system.repository.BackupLogRepository;
import com.aibackup.system.dto.DatabaseRequest;
import org.springframework.stereotype.Service;

import java.io.*;
import java.time.LocalDateTime;

@Service
public class BackupService {

    private final BackupLogRepository repository;

    public BackupService(BackupLogRepository repository) {
        this.repository = repository;
    }

    public String takeBackup(DatabaseRequest db) {

        String backupDir = "C:/backup";
        new File(backupDir).mkdirs();

        String fileName = "backup_" +
                LocalDateTime.now().toString().replace(":", "-") + ".backup";

        try {
            String url = db.getUrl();
            String username = db.getUsername();
            String password = db.getPassword();
            String dbName = url.substring(url.lastIndexOf("/") + 1);

            ProcessBuilder processBuilder;

            // 🔥 DB DETECTION
            if (url.contains("postgresql")) {

                processBuilder = new ProcessBuilder(
                        "C:\\Program Files\\PostgreSQL\\16\\bin\\pg_dump.exe",
                        "-U", username,
                        "-F", "c",
                        "-f", backupDir + "/" + fileName,
                        dbName
                );

                processBuilder.environment().put("PGPASSWORD", password);

            } else if (url.contains("mysql")) {

                processBuilder = new ProcessBuilder(
                        "C:\\Program Files\\MySQL\\MySQL Server 8.0\\bin\\mysqldump.exe",
                        "-u", username,
                        "-p" + password,
                        dbName,
                        "-r", backupDir + "/" + fileName
                );

            } else {
                return "Unsupported DB ❌";
            }

            Process process = processBuilder.start();

            BufferedReader errorReader = new BufferedReader(
                    new InputStreamReader(process.getErrorStream())
            );

            StringBuilder errorOutput = new StringBuilder();
            String line;

            while ((line = errorReader.readLine()) != null) {
                errorOutput.append(line).append("\n");
            }

            int exitCode = process.waitFor();

            BackupLog log = new BackupLog();

            if (exitCode == 0) {
                log.setStatus("SUCCESS");
                log.setMessage("Backup Created: " + fileName);
            } else {
                log.setStatus("FAILED");
                log.setMessage("Error: " + errorOutput);
            }

            log.setTimestamp(LocalDateTime.now());
            repository.save(log);

            return log.getMessage();

        } catch (Exception e) {

            BackupLog log = new BackupLog();
            log.setStatus("FAILED");
            log.setMessage("Exception: " + e.getMessage());
            log.setTimestamp(LocalDateTime.now());
            repository.save(log);

            return "Backup Failed ❌";
        }
    }

    public String restoreBackup(DatabaseRequest db, String fileName) {

        String backupDir = "C:/backup";
        String fullPath = backupDir + "/" + fileName;

        try {
            String url = db.getUrl();
            String username = db.getUsername();
            String password = db.getPassword();
            String dbName = url.substring(url.lastIndexOf("/") + 1);

            ProcessBuilder processBuilder = new ProcessBuilder(
                    "C:\\Program Files\\PostgreSQL\\16\\bin\\pg_restore.exe",
                    "-U", username,
                    "-d", dbName,
                    "-c",
                    fullPath
            );

            processBuilder.environment().put("PGPASSWORD", password);

            Process process = processBuilder.start();

            BufferedReader errorReader = new BufferedReader(
                    new InputStreamReader(process.getErrorStream())
            );

            StringBuilder errorOutput = new StringBuilder();
            String line;

            while ((line = errorReader.readLine()) != null) {
                errorOutput.append(line).append("\n");
            }

            int exitCode = process.waitFor();

            BackupLog log = new BackupLog();

            if (exitCode == 0) {
                log.setStatus("RESTORE_SUCCESS");
                log.setMessage("Database Restored: " + fileName);
            } else {
                log.setStatus("RESTORE_FAILED");
                log.setMessage("Error: " + errorOutput);
            }

            log.setTimestamp(LocalDateTime.now());
            repository.save(log);

            return log.getMessage();

        } catch (Exception e) {

            BackupLog log = new BackupLog();
            log.setStatus("RESTORE_FAILED");
            log.setMessage("Exception: " + e.getMessage());
            log.setTimestamp(LocalDateTime.now());
            repository.save(log);

            return "Restore Failed ❌";
        }
    }
}