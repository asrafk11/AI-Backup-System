package com.aibackup.system.dto;

public class DatabaseRequest {

    private String url;
    private String username;
    private String password;
    private String cronExpression;

    // ==============================
    // 🔹 URL
    // ==============================
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    // ==============================
    // 🔹 USERNAME
    // ==============================
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    // ==============================
    // 🔹 PASSWORD
    // ==============================
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    // ==============================
    // 🔹 CRON EXPRESSION (NEW)
    // ==============================
    public String getCronExpression() {
        return cronExpression;
    }

    public void setCronExpression(String cronExpression) {
        this.cronExpression = cronExpression;
    }
}