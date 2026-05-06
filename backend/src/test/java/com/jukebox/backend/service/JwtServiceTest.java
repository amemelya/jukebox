package com.jukebox.backend.service;

import com.jukebox.backend.config.AppProperties;
import com.jukebox.backend.dto.UserProfile;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JwtServiceTest {

    @Test
    void rejectsMissingJwtSecret() {
        AppProperties properties = new AppProperties();
        properties.setJwtSecret(" ");
        properties.setJwtTtlSeconds(3600L);

        JwtService service = new JwtService(properties);

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> service.issueToken(UserProfile.mobileUser("+911234567890"))
        );

        assertEquals("JWT secret is not configured. Set APP_JWT_SECRET first.", exception.getMessage());
    }

    @Test
    void rejectsNonPositiveTtl() {
        AppProperties properties = new AppProperties();
        properties.setJwtSecret("12345678901234567890123456789012");
        properties.setJwtTtlSeconds(0L);

        JwtService service = new JwtService(properties);

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> service.issueToken(UserProfile.mobileUser("+911234567890"))
        );

        assertEquals("JWT TTL must be greater than zero seconds.", exception.getMessage());
    }
}
