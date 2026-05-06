package com.jukebox.backend.dto;

import javax.validation.constraints.NotBlank;

public class GoogleLoginRequest {

    @NotBlank(message = "Google credential is required.")
    private String credential;

    public String getCredential() {
        return credential;
    }

    public void setCredential(String credential) {
        this.credential = credential;
    }
}
