package com.aibackup.system.scheduler;

import com.aibackup.system.entity.ScheduleConfig;
import com.aibackup.system.entity.DatabaseConfig;
import com.aibackup.system.repository.ScheduleConfigRepository;
import com.aibackup.system.repository.DatabaseConfigRepository;
import com.aibackup.system.service.BackupService;

import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ScheduledFuture;

@Component
public class MultiUserBackupScheduler {

    private final ScheduleConfigRepository scheduleRepo;
    private final DatabaseConfigRepository databaseRepo;
    private final BackupService backupService;
    private final ThreadPoolTaskScheduler taskScheduler;

    private final Map<Long, ScheduledFuture<?>> scheduledTasks = new HashMap<>();

    public MultiUserBackupScheduler(ScheduleConfigRepository scheduleRepo,
                                    DatabaseConfigRepository databaseRepo,
                                    BackupService backupService,
                                    ThreadPoolTaskScheduler taskScheduler) {
        this.scheduleRepo = scheduleRepo;
        this.databaseRepo = databaseRepo;
        this.backupService = backupService;
        this.taskScheduler = taskScheduler;
    }

    // 🔥 Load schedules on startup
    @PostConstruct
    public void init() {
        refreshSchedules();
    }

    // 🔥 Refresh every 1 min
    @org.springframework.scheduling.annotation.Scheduled(fixedDelay = 60000)
    public void refreshSchedules() {

        List<ScheduleConfig> configs = scheduleRepo.findByActiveTrue();

        for (ScheduleConfig config : configs) {

            if (config.getCronExpression() == null || config.getDbId() == null) {
                continue;
            }

            if (scheduledTasks.containsKey(config.getId())) {
                continue;
            }

            try {
                ScheduledFuture<?> future = taskScheduler.schedule(
                        () -> runBackup(config),
                        new CronTrigger(config.getCronExpression())
                );

                scheduledTasks.put(config.getId(), future);

                System.out.println("✅ Scheduled cron for ID: " + config.getId());

            } catch (Exception e) {
                System.out.println("❌ Schedule error: " + e.getMessage());
            }
        }
    }

    // 🔥 CORE LOGIC (UPDATED)
    private void runBackup(ScheduleConfig config) {
        try {
            Optional<DatabaseConfig> dbOpt = databaseRepo.findById(config.getDbId());

            if (dbOpt.isEmpty()) {
                System.out.println("❌ DB not found for ID: " + config.getDbId());
                return;
            }

            DatabaseConfig db = dbOpt.get();

            // ✅ ONLY PASS OBJECT (NO URL BUILD HERE)
            backupService.runBackup(db);

            System.out.println("🚀 Backup executed for schedule ID: " + config.getId());

        } catch (Exception e) {
            System.out.println("❌ Backup failed: " + e.getMessage());
        }
    }
}