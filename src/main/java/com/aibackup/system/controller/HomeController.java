package com.aibackup.system.controller;

import com.aibackup.system.entity.BackupLog;
import com.aibackup.system.repository.BackupLogRepository;
import com.aibackup.system.service.BackupService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class HomeController {

    private final BackupLogRepository repository;
    private final BackupService backupService;

    public HomeController(BackupLogRepository repository, BackupService backupService) {
        this.repository = repository;
        this.backupService = backupService;
    }

    // Test DB Insert
    @GetMapping("/test")
    public String test() {

        BackupLog log = new BackupLog();
        log.setStatus("SUCCESS");
        log.setMessage("Test Backup Entry");

        repository.save(log);

        return "Data Inserted Successfully 🚀";
    }

    // Trigger Backup
    @GetMapping("/backup")
    public String backup() {
        return backupService.takeBackup();
    }

    // View All Backup Logs
    @GetMapping("/logs")
    public List<BackupLog> getLogs() {
        return repository.findAll();
    }
    @GetMapping("/restore")
    public String restore(String file) {
        return backupService.restoreBackup(file);
    }

}


