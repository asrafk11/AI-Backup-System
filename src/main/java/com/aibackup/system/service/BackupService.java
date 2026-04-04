package com.aibackup.system.service;

import com.aibackup.system.entity.BackupLog;
import com.aibackup.system.repository.BackupLogRepository;
import com.aibackup.system.dto.DatabaseRequest;
import org.springframework.stereotype.Service;

import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.time.LocalDateTime;

@Service
public class BackupService {

    private final BackupLogRepository repository;

    public BackupService(BackupLogRepository repository) {
        this.repository = repository;
    }

    // ==============================
    // 🔹 SAVE CONFIG (NEW)
    // ==============================
    public void saveScheduleConfig(DatabaseRequest db) {

        try {
            String query = "INSERT INTO schedule_config " +
                    "(db_url, db_username, db_password, cron_expression, active) " +
                    "VALUES (?, ?, ?, ?, ?)";

            Connection con = DriverManager.getConnection(
                    "jdbc:postgresql://localhost:5432/aibackup",
                    "postgres",
                    "Postgre@2202"
            );

            PreparedStatement ps = con.prepareStatement(query);

            ps.setString(1, db.getUrl());
            ps.setString(2, db.getUsername());
            ps.setString(3, db.getPassword());
            ps.setString(4, db.getCronExpression());
            ps.setBoolean(5, true);

            ps.executeUpdate();

            ps.close();
            con.close();

            System.out.println("✅ Config Saved");

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Save Failed");
        }
    }

    // ==============================
    // 🔹 BACKUP
    // ==============================
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

    // ==============================
    // 🔹 RESTORE
    // ==============================
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
    // ==============================
    // 🔹 MULTI-USER BACKUP (Scheduler)
    // ==============================
    public void performBackup(String dbUrl, String username, String password) {

        DatabaseRequest db = new DatabaseRequest();
        db.setUrl(dbUrl);
        db.setUsername(username);
        db.setPassword(password);

        takeBackup(db); // reuse existing logic
    }
}