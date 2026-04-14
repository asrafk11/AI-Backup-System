package com.aibackup.system.dto;

public class DatabaseRequest {

    private String url;
    private String username;
    private String password;
    private String cronExpression;

    // 🔥 NEW (MULTI-USER + DB LINK)
    private String userId;
    private String dbId;

    // 🔥 NEW (DB TYPE)
    private String dbType;

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
    // 🔹 CRON EXPRESSION
    // ==============================
    public String getCronExpression() {
        return cronExpression;
    }

    public void setCronExpression(String cronExpression) {
        this.cronExpression = cronExpression;
    }

    // ==============================
    // 🔥 USER ID
    // ==============================
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    // ==============================
    // 🔥 DB ID
    // ==============================
    public String getDbId() {
        return dbId;
    }

    public void setDbId(String dbId) {
        this.dbId = dbId;
    }

    // ==============================
    // 🔥 DB TYPE
    // ==============================
    public String getDbType() {
        return dbType;
    }

    public void setDbType(String dbType) {
        this.dbType = dbType;
    }
}