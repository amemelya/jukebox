package com.jukebox.backend.controller;

import com.jukebox.backend.config.AppProperties;
import com.jukebox.backend.dto.ApiResponse;
import com.jukebox.backend.dto.SendOtpRequest;
import com.jukebox.backend.service.GoogleAuthService;
import com.jukebox.backend.service.JwtService;
import com.jukebox.backend.service.OtpService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AuthControllerTest {

    @Test
    void hidesDevOtpWhenExposureIsDisabled() {
        AppProperties properties = new AppProperties();
        properties.setExposeDevOtp(false);

        OtpService otpService = mock(OtpService.class);
        when(otpService.createOtpForPhone(anyString()))
                .thenReturn(new OtpService.OtpCreationResult("123456", System.currentTimeMillis(), "+919876543210"));

        AuthController controller = new AuthController(
                properties,
                otpService,
                mock(GoogleAuthService.class),
                mock(JwtService.class)
        );

        SendOtpRequest request = new SendOtpRequest();
        request.setPhone("9876543210");
        ApiResponse response = controller.sendOtp(request).getBody();

        assertEquals(true, response.isOk());
        assertNull(response.getDevOtp());
    }
}
