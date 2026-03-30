package com.aibackup.system.entity;

import jakarta.persistence.*;

@Entity
public class ScheduleConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String cronExpression;

    // ✅ GETTER
    public String getCronExpression() {
        return cronExpression;
    }

    // ✅ SETTER
    public void setCronExpression(String cronExpression) {
        this.cronExpression = cronExpression;
    }

    public Long getId() {
        return id;
    }
}