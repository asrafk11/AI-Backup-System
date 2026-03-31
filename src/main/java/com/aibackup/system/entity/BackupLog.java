package com.aibackup.system.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "backup_logs")
public class BackupLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String status;

    @Column(length = 2000)
    private String message;

    // 🔥 IMPORTANT (timestamp for sorting + UI)
    @Column(nullable = false)
    private LocalDateTime timestamp;

    // ======================
    // AUTO TIMESTAMP (BEST PRACTICE 🔥)
    // ======================
    @PrePersist
    public void onCreate() {
        this.timestamp = LocalDateTime.now();
    }

    // ======================
    // GETTERS & SETTERS
    // ======================

    public Long getId() {
        return id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}