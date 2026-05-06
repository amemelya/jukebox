package com.jukebox.backend.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

public class VerifyOtpRequest {

    @NotBlank(message = "Phone number is required.")
    private String phone;

    @NotBlank(message = "OTP is required.")
    @Pattern(regexp = "^\\d{6}$", message = "Enter a valid 6-digit OTP.")
    private String otp;

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }
}
