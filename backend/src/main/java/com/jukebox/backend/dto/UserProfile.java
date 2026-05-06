package com.jukebox.backend.dto;

public class UserProfile {

    private final String provider;
    private final String subject;
    private final String name;
    private final String email;
    private final String pictureUrl;
    private final String phoneNumber;

    public UserProfile(String provider, String subject, String name, String email, String pictureUrl, String phoneNumber) {
        this.provider = provider;
        this.subject = subject;
        this.name = name;
        this.email = email;
        this.pictureUrl = pictureUrl;
        this.phoneNumber = phoneNumber;
    }

    public static UserProfile mobileUser(String phoneNumber) {
        return new UserProfile("mobile", phoneNumber, "Jukebox listener", null, null, phoneNumber);
    }

    public String getProvider() {
        return provider;
    }

    public String getSubject() {
        return subject;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPictureUrl() {
        return pictureUrl;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }
}
