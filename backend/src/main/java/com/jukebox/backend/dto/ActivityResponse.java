package com.jukebox.backend.dto;

public class ActivityResponse {

    private final String username;
    private final String action;
    private final String targetId;
    private final String targetTitle;
    private final long createdAt;

    public ActivityResponse(String username, String action, String targetId, String targetTitle, long createdAt) {
        this.username = username;
        this.action = action;
        this.targetId = targetId;
        this.targetTitle = targetTitle;
        this.createdAt = createdAt;
    }

    public String getUsername() {
        return username;
    }

    public String getAction() {
        return action;
    }

    public String getTargetId() {
        return targetId;
    }

    public String getTargetTitle() {
        return targetTitle;
    }

    public long getCreatedAt() {
        return createdAt;
    }
}
