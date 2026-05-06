package com.jukebox.backend.dto;

public class UserSummaryResponse {

    private final String id;
    private final String username;
    private final String displayName;
    private final String pictureUrl;
    private final int followers;
    private final int following;
    private final int listenListSize;

    public UserSummaryResponse(
            String id,
            String username,
            String displayName,
            String pictureUrl,
            int followers,
            int following,
            int listenListSize
    ) {
        this.id = id;
        this.username = username;
        this.displayName = displayName;
        this.pictureUrl = pictureUrl;
        this.followers = followers;
        this.following = following;
        this.listenListSize = listenListSize;
    }

    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getPictureUrl() {
        return pictureUrl;
    }

    public int getFollowers() {
        return followers;
    }

    public int getFollowing() {
        return following;
    }

    public int getListenListSize() {
        return listenListSize;
    }
}
