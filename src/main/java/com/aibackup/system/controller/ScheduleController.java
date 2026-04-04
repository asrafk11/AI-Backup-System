package com.aibackup.system.controller;

import com.aibackup.system.entity.ScheduleConfig;
import com.aibackup.system.repository.ScheduleConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.util.List;

@RestController
@CrossOrigin
public class ScheduleController {

    @Autowired
    private ScheduleConfigRepository repo;

    // 🟢 SAVE NEW SCHEDULE (WITH FULL DATA)
    @PostMapping("/schedule")
    public String saveSchedule(@RequestBody ScheduleConfig config) {

        // ❌ Prevent NULL rows
        if (config.getDbUrl() == null ||
                config.getDbUsername() == null ||
                config.getDbPassword() == null) {

            return "Invalid data ❌";
        }

        config.setActive(true); // default ON

        repo.save(config);

        return "Schedule Saved ✅";
    }

    // 🟢 GET ALL SCHEDULES (IMPORTANT 🔥)
    @GetMapping("/schedules")
    public List<ScheduleConfig> getAllSchedules() {
        return repo.findAll();
    }

    @DeleteMapping("/schedule/{id}")
    public ResponseEntity<String> deleteSchedule(@PathVariable Long id) {
        repo.deleteById(id);
        return ResponseEntity.ok("Deleted ✅");
    }
}