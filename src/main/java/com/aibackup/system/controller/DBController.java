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
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/api")
public class DBController {

    private final BackupService backupService;
    private final String backupDir = "C:/backup";

    public DBController(BackupService backupService) {
        this.backupService = backupService;
    }

    // 🔹 TEST CONNECTION
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
            return "FAILED";
        }
    }

    // 🔹 BACKUP DATABASE (USES SERVICE 🔥)
    @PostMapping("/backup")
    public String backupDB(@RequestBody DatabaseRequest db) {
        return backupService.takeBackup(db);
    }

    // 🔹 RESTORE LATEST BACKUP
    @PostMapping("/restore")
    public String restoreLatest(@RequestBody DatabaseRequest db) {

        try {

            File folder = new File(backupDir);
            File[] files = folder.listFiles();

            if (files == null || files.length == 0) {
                return "NO_BACKUP";
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
            return "ERROR";
        }
    }

    // 🔹 RESTORE SELECTED FILE
    @PostMapping("/restore-selected")
    public String restoreSelected(@RequestBody DatabaseRequest db,
                                  @RequestParam String fileName) {

        try {
            return backupService.restoreBackup(db, fileName);
        } catch (Exception e) {
            e.printStackTrace();
            return "ERROR";
        }
    }

    // 🔹 GET BACKUP FILES
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