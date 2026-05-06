package com.jukebox.backend.model;

public class ActivityEntry {

    private final String userId;
    private final String username;
    private final String action;
    private final String targetId;
    private final String targetTitle;
    private final long createdAt;

    public ActivityEntry(String userId, String username, String action, String targetId, String targetTitle, long createdAt) {
        this.userId = userId;
        this.username = username;
        this.action = action;
        this.targetId = targetId;
        this.targetTitle = targetTitle;
        this.createdAt = createdAt;
    }

    public String getUserId() {
        return userId;
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
