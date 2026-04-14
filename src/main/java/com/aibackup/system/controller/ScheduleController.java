package com.aibackup.system.controller;

import com.aibackup.system.entity.ScheduleConfig;
import com.aibackup.system.repository.ScheduleConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:3000")
public class ScheduleController {

    @Autowired
    private ScheduleConfigRepository repo;

    // 🟢 CREATE SCHEDULE
    @PostMapping("/schedule")
    public ResponseEntity<?> saveSchedule(@RequestBody ScheduleConfig config) {

        if (config.getUserId() == null ||
                config.getDbId() == null ||
                config.getCronExpression() == null) {

            return ResponseEntity.badRequest().body(
                    Map.of("success", false, "message", "Invalid data")
            );
        }

        config.setActive(true);
        repo.save(config);

        return ResponseEntity.ok(
                Map.of("success", true)
        );
    }

    // 🔥 FIXED: GET ALL SCHEDULES (NO PARAM)
    @GetMapping("/schedules")
    public List<ScheduleConfig> getAllSchedules() {
        return repo.findAll();
    }

    // 🟢 OPTIONAL: GET BY USER (if needed later)
    @GetMapping("/schedules/user")
    public List<ScheduleConfig> getSchedulesByUser(@RequestParam UUID userId) {
        return repo.findByUserId(userId);
    }

    // 🟢 DELETE
    @DeleteMapping("/schedule/{id}")
    public ResponseEntity<?> deleteSchedule(@PathVariable Long id) {

        if (!repo.existsById(id)) {
            return ResponseEntity.status(404).body(
                    Map.of("success", false, "message", "Schedule not found")
            );
        }

        repo.deleteById(id);

        return ResponseEntity.ok(
                Map.of("success", true, "message", "Deleted successfully")
        );
    }
}