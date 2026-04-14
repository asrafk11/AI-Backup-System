package com.aibackup.system.repository;

import java.util.List;
import java.util.List;
import java.util.UUID;
import com.aibackup.system.entity.ScheduleConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScheduleConfigRepository
        extends JpaRepository<ScheduleConfig, Long> {

    List<ScheduleConfig> findByUserId(UUID userId);

    // 🔥 Get all ACTIVE users only (best for scheduler)
    List<ScheduleConfig> findByActiveTrue();

    // (Optional) Keep this if you still need latest config somewhere
    ScheduleConfig findTopByOrderByIdDesc();
}