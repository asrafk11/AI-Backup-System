package com.aibackup.system.scheduler;

import com.aibackup.system.entity.ScheduleConfig;
import com.aibackup.system.repository.ScheduleConfigRepository;
import com.aibackup.system.service.BackupService;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.List;

@Component
public class MultiUserBackupScheduler {

    private final ScheduleConfigRepository scheduleRepo;
    private final BackupService backupService;
    private final ThreadPoolTaskScheduler taskScheduler;

    public MultiUserBackupScheduler(ScheduleConfigRepository scheduleRepo,
                                    BackupService backupService,
                                    ThreadPoolTaskScheduler taskScheduler) {
        this.scheduleRepo = scheduleRepo;
        this.backupService = backupService;
        this.taskScheduler = taskScheduler;
    }

    // 🔥 Runs once when app starts
    @PostConstruct
    public void scheduleAllUsers() {

        List<ScheduleConfig> configs = scheduleRepo.findByActiveTrue();

        for (ScheduleConfig config : configs) {

            String cron = config.getCronExpression();

            taskScheduler.schedule(
                    () -> runBackup(config),
                    new CronTrigger(cron)
            );
        }
    }

    private void runBackup(ScheduleConfig config) {

        try {
            backupService.performBackup(
                    config.getDbUrl(),
                    config.getDbUsername(),
                    config.getDbPassword()
            );
        } catch (Exception e) {
            // no crash — continues other users
        }
    }
}