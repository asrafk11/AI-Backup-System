package com.aibackup.system.controller;

import com.aibackup.system.dto.DatabaseRequest;
import com.aibackup.system.service.BackupService;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.List;

@RestController
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
@RequestMapping("/api")
public class DBController {

    private final BackupService backupService;
    private final String backupDir = "C:/backup";

    public DBController(BackupService backupService) {
        this.backupService = backupService;
    }

    // ==============================
    // 🔹 SAVE CONFIG (NEW)
    // ==============================
    @PostMapping("/save-config")
    public String saveConfig(@RequestBody DatabaseRequest db) {
        try {
            backupService.saveScheduleConfig(db);
            return "SAVED";
        } catch (Exception e) {
            e.printStackTrace();
            return "FAILED: " + e.getMessage();
        }
    }

    // ==============================
    // 🔹 TEST CONNECTION
    // ==============================
    @PostMapping("/connect")
    public String connectDB(@RequestBody DatabaseRequest db) {
        try {
            Connection con = DriverManager.getConnection(
                    db.getUrl(),
                    db.getUsername(),
                    db.getPassword()
            );
            con.close();
            return "SUCCESS";
        } catch (Exception e) {
            e.printStackTrace();
            return "FAILED: " + e.getMessage();
        }
    }

    // ==============================
    // 🔹 BACKUP DATABASE
    // ==============================
    @PostMapping("/backup")
    public String backupDB(@RequestBody DatabaseRequest db) {
        try {
            return backupService.takeBackup(db);
        } catch (Exception e) {
            e.printStackTrace();
            return "BACKUP FAILED: " + e.getMessage();
        }
    }

    // ==============================
    // 🔹 RESTORE LATEST BACKUP
    // ==============================
    @PostMapping("/restore")
    public String restoreLatest(@RequestBody DatabaseRequest db) {
        try {
            File folder = new File(backupDir);
            File[] files = folder.listFiles();

            if (files == null || files.length == 0) {
                return "NO_BACKUP_FOUND";
            }

            File latestFile = files[0];

            for (File f : files) {
                if (f.lastModified() > latestFile.lastModified()) {
                    latestFile = f;
                }
            }

            return backupService.restoreBackup(db, latestFile.getName());

        } catch (Exception e) {
            e.printStackTrace();
            return "RESTORE FAILED: " + e.getMessage();
        }
    }

    // ==============================
    // 🔹 GET BACKUP FILES
    // ==============================
    @GetMapping("/backups")
    public List<String> getBackupFiles() {
        File folder = new File(backupDir);
        File[] files = folder.listFiles();

        List<String> fileNames = new ArrayList<>();

        if (files != null) {
            for (File file : files) {
                fileNames.add(file.getName());
            }
        }

        return fileNames;
    }
}