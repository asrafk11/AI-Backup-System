package com.aibackup.system.entity;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "schedule_config")
public class ScheduleConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 🔥 USER (multi-user support)
    @Column(name = "user_id")
    private UUID userId;

    // 🔥 DATABASE LINK (IMPORTANT)
    @Column(name = "db_id")
    private UUID dbId;

    // 🔥 SCHEDULE
    private String cronExpression;

    // 🔥 ACTIVE / INACTIVE
    private boolean active = true;

    // ======================
    // GETTERS & SETTERS
    // ======================

    public Long getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public UUID getDbId() {
        return dbId;
    }

    public void setDbId(UUID dbId) {
        this.dbId = dbId;
    }

    public String getCronExpression() {
        return cronExpression;
    }

    public void setCronExpression(String cronExpression) {
        this.cronExpression = cronExpression;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}