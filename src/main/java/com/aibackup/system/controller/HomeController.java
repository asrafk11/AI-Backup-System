package com.aibackup.system.controller;

import com.aibackup.system.entity.BackupLog;
import com.aibackup.system.repository.BackupLogRepository;
import com.aibackup.system.service.BackupService;
import com.aibackup.system.dto.DatabaseRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Sort;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
public class HomeController {

    private final BackupLogRepository repository;
    private final BackupService backupService;

    // 🔥 Schedule control flag
    private boolean isScheduleActive = true;

    public HomeController(BackupLogRepository repository, BackupService backupService) {
        this.repository = repository;
        this.backupService = backupService;
    }

    // =========================
    // 🔹 BACKUP
    // =========================
    @PostMapping("/backup")
    public String backup(@RequestBody DatabaseRequest db) {
        return backupService.takeBackup(db);
    }

    // =========================
    // 🔹 RESTORE
    // =========================
    @PostMapping("/restore")
    public String restore(@RequestBody DatabaseRequest db,
                          @RequestParam String file) {
        return backupService.restoreBackup(db, file);
    }

    // =========================
    // 🔹 LOGS (SORTED 🔥)
    // =========================
    @GetMapping("/logs")
    public List<BackupLog> getLogs() {
        return repository.findAll(Sort.by(Sort.Direction.DESC, "timestamp"));
    }

    // =========================
    // 🔹 DB STATUS
    // =========================
    @PostMapping("/status")
    public String checkStatus(@RequestBody DatabaseRequest db) {
        try {
            Connection con = DriverManager.getConnection(
                    db.getUrl(),
                    db.getUsername(),
                    db.getPassword()
            );
            con.close();
            return "UP";
        } catch (Exception e) {
            return "DOWN";
        }
    }

    // =========================
    // 🔥 CANCEL SCHEDULE
    // =========================
    @PostMapping("/cancel-schedule")
    public String cancelSchedule() {
        isScheduleActive = false;
        return "Schedule Cancelled Successfully ❌";
    }

    // =========================
    // 🔥 START SCHEDULE (OPTIONAL)
    // =========================
    @PostMapping("/start-schedule")
    public String startSchedule() {
        isScheduleActive = true;
        return "Schedule Started Successfully ✅";
    }

    // =========================
    // 🔥 CHECK SCHEDULE STATUS
    // =========================
    @GetMapping("/schedule-status")
    public String getScheduleStatus() {
        return isScheduleActive ? "ACTIVE" : "INACTIVE";
    }
}