package com.aibackup.system.controller;

import com.aibackup.system.entity.ScheduleConfig;
import com.aibackup.system.repository.ScheduleConfigRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
public class ScheduleController {

    @Autowired
    private ScheduleConfigRepository repo;

    @PostMapping("/schedule")
    public String updateSchedule(@RequestBody String cron) {

        ScheduleConfig config = new ScheduleConfig();
        config.setCronExpression(cron);

        repo.save(config);

        return "Schedule Updated ✅";
    }
}