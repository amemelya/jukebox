package com.jukebox.backend.service;

import com.jukebox.backend.config.AppProperties;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.client.RestTemplateBuilder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertFalse;

class OtpServiceTest {

    @Test
    void normalizesTenDigitMobileNumberUsingConfiguredCountryCode() {
        AppProperties properties = new AppProperties();
        properties.setSmsProvider("mock");
        properties.setDefaultCountryCode("+91");

        OtpService service = new OtpService(properties, new RestTemplateBuilder());

        OtpService.OtpCreationResult result = service.createOtpForPhone("9876543210");

        assertEquals("+919876543210", result.getNormalizedPhone());
        assertNotNull(result.getCode());
    }

    @Test
    void acceptsFormattedInternationalPhoneNumber() {
        AppProperties properties = new AppProperties();
        properties.setSmsProvider("mock");
        properties.setDefaultCountryCode("+91");

        OtpService service = new OtpService(properties, new RestTemplateBuilder());

        OtpService.OtpCreationResult result = service.createOtpForPhone("+1 (415) 555-2671");

        assertEquals("+14155552671", result.getNormalizedPhone());
    }

    @Test
    void rejectsInvalidLocalPhoneNumber() {
        AppProperties properties = new AppProperties();
        properties.setSmsProvider("mock");
        properties.setDefaultCountryCode("+91");

        OtpService service = new OtpService(properties, new RestTemplateBuilder());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.createOtpForPhone("12345")
        );

        assertEquals("Enter a valid 10-digit mobile number.", exception.getMessage());
    }

    @Test
    void invalidOtpIsRejectedAfterFiveAttempts() {
        AppProperties properties = new AppProperties();
        properties.setSmsProvider("mock");
        properties.setDefaultCountryCode("+91");

        OtpService service = new OtpService(properties, new RestTemplateBuilder());
        OtpService.OtpCreationResult result = service.createOtpForPhone("9876543210");

        for (int attempt = 1; attempt < 5; attempt++) {
            OtpService.OtpVerificationResult verificationResult = service.verifyOtpForPhone(
                    result.getNormalizedPhone(),
                    "000000"
            );

            assertFalse(verificationResult.isOk());
            assertEquals("Incorrect OTP. Try again.", verificationResult.getMessage());
        }

        OtpService.OtpVerificationResult lockedResult = service.verifyOtpForPhone(
                result.getNormalizedPhone(),
                "000000"
        );

        assertFalse(lockedResult.isOk());
        assertEquals("Too many incorrect OTP attempts. Request a new code.", lockedResult.getMessage());
        assertEquals(
                "No OTP request found for this number.",
                service.verifyOtpForPhone(result.getNormalizedPhone(), result.getCode()).getMessage()
        );
    }
}
