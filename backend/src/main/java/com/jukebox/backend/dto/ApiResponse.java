package com.jukebox.backend.dto;

public class ApiResponse {

    private final boolean ok;
    private final String message;
    private final String devOtp;
    private final String token;
    private final UserProfile user;

    public ApiResponse(boolean ok, String message, String devOtp, String token, UserProfile user) {
        this.ok = ok;
        this.message = message;
        this.devOtp = devOtp;
        this.token = token;
        this.user = user;
    }

    public boolean isOk() {
        return ok;
    }

    public String getMessage() {
        return message;
    }

    public String getDevOtp() {
        return devOtp;
    }

    public String getToken() {
        return token;
    }

    public UserProfile getUser() {
        return user;
    }
}
