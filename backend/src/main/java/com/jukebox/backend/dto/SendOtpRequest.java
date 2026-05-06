package com.jukebox.backend.dto;

import javax.validation.constraints.NotBlank;

public class SendOtpRequest {

    @NotBlank(message = "Phone number is required.")
    private String phone;

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
