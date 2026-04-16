package com.aibackup.system.service;

import com.aibackup.system.entity.BackupLog;
import com.aibackup.system.entity.DatabaseConfig;
import com.aibackup.system.repository.BackupLogRepository;
import com.aibackup.system.dto.DatabaseRequest;
import org.springframework.stereotype.Service;
import com.aibackup.system.config.DynamicDBConnection;
import com.aibackup.system.repository.DatabaseConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class BackupService {

    private final BackupLogRepository repository;

    @Autowired
    private DatabaseConfigRepository databaseConfigRepository;

    public BackupService(BackupLogRepository repository) {
        this.repository = repository;
    }

    // ==============================
    // 🔹 SAVE CONFIG
    // ==============================
    public void saveScheduleConfig(DatabaseRequest db) {

        try {
            String query = "INSERT INTO schedule_config " +
                    "(user_id, db_id, cron_expression, active) " +
                    "VALUES (?, ?, ?, ?)";

            Connection con = DriverManager.getConnection(
                    "jdbc:postgresql://localhost:5432/aibackup",
                    "postgres",
                    "Postgre@2202"
            );

            PreparedStatement ps = con.prepareStatement(query);

            ps.setObject(1, java.util.UUID.fromString(db.getUserId()));
            ps.setObject(2, java.util.UUID.fromString(db.getDbId()));
            ps.setString(3, db.getCronExpression());
            ps.setBoolean(4, true);

            ps.executeUpdate();

            ps.close();
            con.close();

            System.out.println("✅ Schedule Saved with DB link");

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
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

            // ✅ FIX: prevent null crash
            if (url == null || url.isEmpty()) {
                return "Invalid DB URL ❌";
            }

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
    // 🔹 MANUAL BACKUP USING dbId
    // ==============================
    public String runManualBackup(UUID dbId) {

        try {
            DatabaseConfig db = databaseConfigRepository.findById(dbId)
                    .orElseThrow(() -> new RuntimeException("DB not found"));

            String url;

            if ("postgres".equalsIgnoreCase(db.getDbType())) {
                url = "jdbc:postgresql://" + db.getHost() + ":" + db.getPort() + "/" + db.getDbName();
            } else if ("mysql".equalsIgnoreCase(db.getDbType())) {
                url = "jdbc:mysql://" + db.getHost() + ":" + db.getPort() + "/" + db.getDbName();
            } else {
                throw new RuntimeException("Unsupported DB type");
            }

            DatabaseRequest request = new DatabaseRequest();
            request.setUrl(url);
            request.setUsername(db.getUsername());
            request.setPassword(db.getPassword());
            request.setDbType(db.getDbType());

            return takeBackup(request);

        } catch (Exception e) {
            return "Manual Backup Failed ❌ " + e.getMessage();
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

            ProcessBuilder processBuilder;

            // 🔹 PostgreSQL Restore (FIXED)
            if (url.contains("postgresql")) {

                processBuilder = new ProcessBuilder(
                        "C:\\Program Files\\PostgreSQL\\16\\bin\\pg_restore.exe",
                        "-U", username,
                        "-d", dbName,
                        "-F", "c",                      // ✅ IMPORTANT FIX
                        "--clean",
                        "--if-exists",
                        fullPath
                );

                processBuilder.environment().put("PGPASSWORD", password);

                // 🔹 MySQL Restore (SAFE VERSION)
            } else if (url.contains("mysql")) {

                processBuilder = new ProcessBuilder(
                        "cmd.exe", "/c",
                        "\"C:\\Program Files\\MySQL\\MySQL Server 8.0\\bin\\mysql.exe\" -u "
                                + username + " -p" + password + " " + dbName + " < \"" + fullPath + "\""
                );

            } else {
                return "Unsupported DB ❌";
            }

            System.out.println("Running restore for DB: " + dbName);
            System.out.println("Backup file: " + fullPath);

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

            System.out.println("Restore Exit Code: " + exitCode);
            System.out.println("Restore Errors: " + errorOutput);

            // ✅ STRICT SUCCESS CHECK
            if (exitCode == 0 && errorOutput.length() == 0) {
                log.setStatus("RESTORE_SUCCESS");
                log.setMessage("Database Restored: " + fileName);
            } else if (exitCode == 0) {
                log.setStatus("RESTORE_SUCCESS");
                log.setMessage("Restored with warnings: " + fileName);
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
    public void runBackup(DatabaseConfig db) {

        try {
            Connection connection = DynamicDBConnection.getConnection(db);

            System.out.println("✅ Connected to DB: " + db.getDbName());

            DatabaseRequest request = new DatabaseRequest();

            String url;

            if ("postgres".equalsIgnoreCase(db.getDbType())) {
                url = "jdbc:postgresql://" + db.getHost() + ":" + db.getPort() + "/" + db.getDbName();
            } else if ("mysql".equalsIgnoreCase(db.getDbType())) {
                url = "jdbc:mysql://" + db.getHost() + ":" + db.getPort() + "/" + db.getDbName();
            } else {
                throw new RuntimeException("Unsupported DB type");
            }

            request.setUrl(url);
            request.setUsername(db.getUsername());
            request.setPassword(db.getPassword());
            request.setDbType(db.getDbType());

            takeBackup(request);

            connection.close();

        } catch (Exception e) {
            System.out.println("❌ Dynamic backup failed: " + e.getMessage());
        }
    }
}