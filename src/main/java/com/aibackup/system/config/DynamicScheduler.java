package com.aibackup.system.config;

import com.aibackup.system.repository.ScheduleConfigRepository;
import com.aibackup.system.entity.ScheduleConfig;
import com.aibackup.system.service.BackupService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.support.CronTrigger;

@Configuration
@EnableScheduling
public class DynamicScheduler implements SchedulingConfigurer {

    @Autowired
    private BackupService backupService;

    @Autowired
    private ScheduleConfigRepository repo;

    @Override
    public void configureTasks(ScheduledTaskRegistrar registrar) {

        registrar.addTriggerTask(
                () -> backupService.takeBackup(),
                triggerContext -> {

                    ScheduleConfig config = repo.findTopByOrderByIdDesc();

                    if (config == null || config.getCronExpression() == null) {
                        return null; // no schedule set yet
                    }

                    return new CronTrigger(config.getCronExpression())
                            .nextExecution(triggerContext);
                }
        );
    }
}