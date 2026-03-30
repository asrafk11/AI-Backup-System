package com.aibackup.system.repository;

import com.aibackup.system.entity.ScheduleConfig;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScheduleConfigRepository
        extends JpaRepository<ScheduleConfig, Long> {

    ScheduleConfig findTopByOrderByIdDesc();
}